package com.berlin.aetherflow.system.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.berlin.aetherflow.config.ServletUtils;
import com.berlin.aetherflow.exception.ApiException;
import com.berlin.aetherflow.system.user.domain.bo.AuthLoginBo;
import com.berlin.aetherflow.system.user.domain.bo.AuthRefreshBo;
import com.berlin.aetherflow.system.user.domain.bo.AuthRegisterBo;
import com.berlin.aetherflow.system.user.domain.entity.SysPermission;
import com.berlin.aetherflow.system.user.domain.entity.SysRole;
import com.berlin.aetherflow.system.user.domain.entity.SysRolePermission;
import com.berlin.aetherflow.system.user.domain.entity.SysUser;
import com.berlin.aetherflow.system.user.domain.entity.SysUserRole;
import com.berlin.aetherflow.system.user.domain.vo.AuthLoginVo;
import com.berlin.aetherflow.system.user.domain.vo.AuthUserInfoVo;
import com.berlin.aetherflow.system.user.mapper.SysPermissionMapper;
import com.berlin.aetherflow.system.user.mapper.SysRoleMapper;
import com.berlin.aetherflow.system.user.mapper.SysRolePermissionMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserRoleMapper;
import com.berlin.aetherflow.system.user.service.AuthService;
import com.berlin.aetherflow.system.user.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 认证与授权服务实现。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int USER_STATUS_NORMAL = 0;
    private static final int USER_STATUS_DISABLED = 1;
    private static final int USER_STATUS_LOCKED = 2;
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 5;
    private static final String DEFAULT_ROLE_KEY = "operator";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String REFRESH_TOKEN_VERSION = "v1";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SecurityAuditService securityAuditService;

    @Value("${aether-flow.auth.refresh-token.secret:aether-flow-local-refresh-secret}")
    private String refreshTokenSecret;

    @Value("${aether-flow.auth.refresh-token.ttl-seconds:604800}")
    private long refreshTokenTtlSeconds;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(AuthRegisterBo bo) {
        String username = normalizeRequired(bo == null ? null : bo.getUsername(), "用户名不能为空");
        String rawPassword = normalizeRequired(bo == null ? null : bo.getPassword(), "密码不能为空");
        String email = normalizeOptional(bo == null ? null : bo.getEmail());

        if (rawPassword.length() < 6) {
            throw ApiException.badRequest("密码长度不能小于6位");
        }

        if (sysUserMapper.selectByColumn(SysUser::getUsername, username) != null) {
            throw ApiException.business("用户名已存在");
        }
        if (StringUtils.isNotBlank(email) && sysUserMapper.selectByColumn(SysUser::getEmail, email) != null) {
            throw ApiException.business("邮箱已被占用");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(BCrypt.hashpw(rawPassword));
        user.setNickname(StringUtils.defaultIfBlank(normalizeOptional(bo.getNickname()), username));
        user.setEmail(email);
        user.setPhone(normalizeOptional(bo.getPhone()));
        user.setStatus(USER_STATUS_NORMAL);
        user.setLoginFailCount(0);
        user.setLockUntil(null);
        user.setLastLoginTime(null);

        int inserted = sysUserMapper.insert(user);
        if (inserted <= 0 || user.getId() == null) {
            throw ApiException.business("注册失败，请稍后重试");
        }

        bindDefaultRole(user.getId());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVo login(AuthLoginBo bo, HttpServletRequest request) {
        String requestUri = request == null ? null : request.getRequestURI();
        String clientIp = request == null ? null : ServletUtils.getClientIpAddress(request);
        String username = null;
        SysUser user = null;

        try {
            username = normalizeRequired(bo == null ? null : bo.getUsername(), "用户名不能为空");
            String rawPassword = normalizeRequired(bo == null ? null : bo.getPassword(), "密码不能为空");

            user = sysUserMapper.selectByColumn(SysUser::getUsername, username);
            if (user == null || StringUtils.isBlank(user.getPasswordHash())) {
                throw ApiException.unauthorized("账号或密码错误");
            }
            if (Objects.equals(user.getStatus(), USER_STATUS_DISABLED)) {
                throw ApiException.forbidden("账号已停用，请联系管理员");
            }
            checkAndRepairLockStatus(user);

            if (!BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
                handleLoginFail(user);
            }

            handleLoginSuccess(user);
            StpUtil.login(user.getId());
            StpUtil.getTokenSession().set("operatorName", resolveOperatorName(user));

            AuthLoginVo loginVo = buildLoginVo(user);

            securityAuditService.record(
                    user.getId(),
                    user.getUsername(),
                    "LOGIN",
                    "USER_LOGIN",
                    requestUri,
                    clientIp,
                    1,
                    "登录成功"
            );
            return loginVo;
        } catch (RuntimeException ex) {
            securityAuditService.record(
                    user == null ? null : user.getId(),
                    user == null ? username : user.getUsername(),
                    "LOGIN",
                    "USER_LOGIN",
                    requestUri,
                    clientIp,
                    0,
                    ex.getMessage()
            );
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthLoginVo refresh(AuthRefreshBo bo, HttpServletRequest request) {
        String requestUri = request == null ? null : request.getRequestURI();
        String clientIp = request == null ? null : ServletUtils.getClientIpAddress(request);
        String username = null;
        Long userId = null;

        try {
            String refreshToken = normalizeRequired(bo == null ? null : bo.getRefreshToken(), "刷新令牌不能为空");
            RefreshTokenClaims claims = parseRefreshToken(refreshToken);
            userId = claims.userId();

            SysUser user = sysUserMapper.selectById(claims.userId());
            if (user == null) {
                throw ApiException.unauthorized("刷新令牌已失效，请重新登录");
            }

            username = user.getUsername();
            if (Objects.equals(user.getStatus(), USER_STATUS_DISABLED)) {
                throw ApiException.forbidden("账号已停用，请联系管理员");
            }
            checkAndRepairLockStatus(user);
            validateRefreshTokenSignature(claims, user);

            StpUtil.login(user.getId());
            StpUtil.getTokenSession().set("operatorName", resolveOperatorName(user));

            AuthLoginVo loginVo = buildLoginVo(user);
            securityAuditService.record(
                    user.getId(),
                    user.getUsername(),
                    "LOGIN",
                    "TOKEN_REFRESH",
                    requestUri,
                    clientIp,
                    1,
                    "刷新令牌成功"
            );
            return loginVo;
        } catch (RuntimeException ex) {
            securityAuditService.record(
                    userId,
                    username,
                    "LOGIN",
                    "TOKEN_REFRESH",
                    requestUri,
                    clientIp,
                    0,
                    ex.getMessage()
            );
            throw ex;
        }
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public List<String> getRoleKeysByUserId(Long userId) {
        Set<Long> roleIds = getActiveRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }

        return sysRoleMapper.selectByIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.equals(role.getStatus(), USER_STATUS_NORMAL))
                .map(SysRole::getRoleKey)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }

    @Override
    public List<String> getPermissionKeysByUserId(Long userId) {
        Set<Long> roleIds = getActiveRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<SysRolePermission> rolePermissionQuery = new LambdaQueryWrapper<>();
        rolePermissionQuery.in(SysRolePermission::getRoleId, roleIds);
        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(rolePermissionQuery);
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return List.of();
        }

        Set<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (permissionIds.isEmpty()) {
            return List.of();
        }

        return sysPermissionMapper.selectByIds(permissionIds).stream()
                .filter(Objects::nonNull)
                .filter(permission -> Objects.equals(permission.getStatus(), USER_STATUS_NORMAL))
                .map(SysPermission::getPermKey)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }

    private AuthLoginVo buildLoginVo(SysUser user) {
        AuthLoginVo loginVo = new AuthLoginVo();
        loginVo.setToken(StpUtil.getTokenValue());
        loginVo.setRefreshToken(issueRefreshToken(user.getId()));
        loginVo.setRoles(getRoleKeysByUserId(user.getId()));
        loginVo.setUserInfo(buildUserInfo(user));
        return loginVo;
    }

    private AuthUserInfoVo buildUserInfo(SysUser user) {
        AuthUserInfoVo userInfo = new AuthUserInfoVo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        return userInfo;
    }

    private String resolveOperatorName(SysUser user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.isNotBlank(user.getNickname())) {
            return user.getNickname();
        }
        if (StringUtils.isNotBlank(user.getUsername())) {
            return user.getUsername();
        }
        return String.valueOf(user.getId());
    }

    private String issueRefreshToken(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || StringUtils.isBlank(user.getPasswordHash())) {
            throw ApiException.unauthorized("刷新令牌签发失败，请重新登录");
        }
        long expireAt = Instant.now().getEpochSecond() + refreshTokenTtlSeconds;
        String nonce = generateNonce();
        String payload = String.join(":", REFRESH_TOKEN_VERSION, String.valueOf(userId), String.valueOf(expireAt), nonce);
        String encodedPayload = encodeBase64Url(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + signPayload(payload, user);
    }

    private RefreshTokenClaims parseRefreshToken(String refreshToken) {
        String[] tokenParts = refreshToken.split("\\.", 2);
        if (tokenParts.length != 2 || StringUtils.isBlank(tokenParts[0]) || StringUtils.isBlank(tokenParts[1])) {
            throw ApiException.unauthorized("刷新令牌已失效，请重新登录");
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[0]), StandardCharsets.UTF_8);
            String[] payloadParts = payload.split(":", 4);
            if (payloadParts.length != 4 || !REFRESH_TOKEN_VERSION.equals(payloadParts[0])) {
                throw ApiException.unauthorized("刷新令牌已失效，请重新登录");
            }
            Long userId = Long.parseLong(payloadParts[1]);
            long expireAt = Long.parseLong(payloadParts[2]);
            if (expireAt <= Instant.now().getEpochSecond()) {
                throw ApiException.unauthorized("刷新令牌已失效，请重新登录");
            }
            return new RefreshTokenClaims(payload, tokenParts[1], userId);
        } catch (NumberFormatException ex) {
            throw ApiException.unauthorized("刷新令牌已失效，请重新登录");
        } catch (IllegalArgumentException ex) {
            throw ApiException.unauthorized("刷新令牌已失效，请重新登录");
        }
    }

    private void validateRefreshTokenSignature(RefreshTokenClaims claims, SysUser user) {
        String expectedSignature = signPayload(claims.payload(), user);
        byte[] expected = expectedSignature.getBytes(StandardCharsets.UTF_8);
        byte[] actual = claims.signature().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw ApiException.unauthorized("刷新令牌已失效，请重新登录");
        }
    }

    private String signPayload(String payload, SysUser user) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            String key = refreshTokenSecret + ":" + user.getPasswordHash();
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return encodeBase64Url(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("刷新令牌签名失败", ex);
        }
    }

    private String generateNonce() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return encodeBase64Url(bytes);
    }

    private String encodeBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void bindDefaultRole(Long userId) {
        SysRole operatorRole = sysRoleMapper.selectByColumn(SysRole::getRoleKey, DEFAULT_ROLE_KEY);
        if (operatorRole == null || !Objects.equals(operatorRole.getStatus(), USER_STATUS_NORMAL)) {
            throw ApiException.business("系统默认角色不存在，请联系管理员");
        }
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(operatorRole.getId());
        sysUserRoleMapper.insert(relation);
    }

    private Set<Long> getActiveRoleIdsByUserId(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        List<SysUserRole> relations = sysUserRoleMapper.selectListByColumn(SysUserRole::getUserId, userId);
        if (relations == null || relations.isEmpty()) {
            return Set.of();
        }

        Set<Long> relationRoleIds = relations.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (relationRoleIds.isEmpty()) {
            return Set.of();
        }

        return filterActiveRoleIds(relationRoleIds);
    }

    private Set<Long> filterActiveRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return sysRoleMapper.selectByIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.equals(role.getStatus(), USER_STATUS_NORMAL))
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void handleLoginSuccess(SysUser user) {
        SysUser toUpdate = new SysUser();
        toUpdate.setId(user.getId());
        toUpdate.setLoginFailCount(0);
        toUpdate.setLockUntil(null);
        toUpdate.setLastLoginTime(LocalDateTime.now());
        if (Objects.equals(user.getStatus(), USER_STATUS_LOCKED)) {
            toUpdate.setStatus(USER_STATUS_NORMAL);
            user.setStatus(USER_STATUS_NORMAL);
        }
        sysUserMapper.updateById(toUpdate);
        user.setLoginFailCount(0);
        user.setLockUntil(null);
    }

    private void handleLoginFail(SysUser user) {
        int failCount = user.getLoginFailCount() == null ? 1 : user.getLoginFailCount() + 1;
        SysUser toUpdate = new SysUser();
        toUpdate.setId(user.getId());
        toUpdate.setLoginFailCount(failCount);

        if (failCount >= MAX_LOGIN_FAIL_COUNT) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            toUpdate.setStatus(USER_STATUS_LOCKED);
            toUpdate.setLockUntil(lockUntil);
            sysUserMapper.updateById(toUpdate);
            throw ApiException.unauthorized("账号已锁定，请" + LOCK_MINUTES + "分钟后重试");
        }

        sysUserMapper.updateById(toUpdate);
        int remaining = MAX_LOGIN_FAIL_COUNT - failCount;
        throw ApiException.unauthorized("账号或密码错误，剩余尝试次数：" + remaining);
    }

    private void checkAndRepairLockStatus(SysUser user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockUntil = user.getLockUntil();

        if (lockUntil != null && lockUntil.isAfter(now)) {
            throw ApiException.unauthorized("账号已锁定，请稍后重试");
        }
        if (Objects.equals(user.getStatus(), USER_STATUS_LOCKED) && lockUntil == null) {
            throw ApiException.forbidden("账号已锁定，请联系管理员");
        }

        if (lockUntil != null && !lockUntil.isAfter(now)) {
            SysUser toUpdate = new SysUser();
            toUpdate.setId(user.getId());
            toUpdate.setStatus(USER_STATUS_NORMAL);
            toUpdate.setLoginFailCount(0);
            toUpdate.setLockUntil(null);
            sysUserMapper.updateById(toUpdate);
            user.setStatus(USER_STATUS_NORMAL);
            user.setLoginFailCount(0);
            user.setLockUntil(null);
        }
    }

    private String normalizeRequired(String input, String message) {
        String normalized = normalizeOptional(input);
        if (StringUtils.isBlank(normalized)) {
            throw ApiException.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }

    private record RefreshTokenClaims(String payload, String signature, Long userId) {
    }
}

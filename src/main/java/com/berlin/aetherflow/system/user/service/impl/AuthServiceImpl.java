package com.berlin.aetherflow.system.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.berlin.aetherflow.config.ServletUtils;
import com.berlin.aetherflow.exception.ApiException;
import com.berlin.aetherflow.system.user.domain.bo.AuthLoginBo;
import com.berlin.aetherflow.system.user.domain.bo.AuthRegisterBo;
import com.berlin.aetherflow.system.user.domain.entity.SysRole;
import com.berlin.aetherflow.system.user.domain.entity.SysUser;
import com.berlin.aetherflow.system.user.domain.entity.SysUserRole;
import com.berlin.aetherflow.system.user.domain.vo.AuthLoginVo;
import com.berlin.aetherflow.system.user.domain.vo.AuthUserInfoVo;
import com.berlin.aetherflow.system.user.mapper.SysRoleMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserRoleMapper;
import com.berlin.aetherflow.system.user.service.AuthService;
import com.berlin.aetherflow.system.user.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证与授权服务实现。
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int USER_STATUS_NORMAL = 0;
    private static final int USER_STATUS_DISABLED = 1;
    private static final int USER_STATUS_LOCKED = 2;
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final int LOCK_MINUTES = 5;
    private static final String DEFAULT_ROLE_KEY = "operator";

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SecurityAuditService securityAuditService;

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

            AuthLoginVo loginVo = new AuthLoginVo();
            loginVo.setToken(StpUtil.getTokenValue());
            loginVo.setRoles(getRoleKeysByUserId(user.getId()));
            loginVo.setUserInfo(buildUserInfo(user));

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
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public List<String> getRoleKeysByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<SysUserRole> relations = sysUserRoleMapper.selectListByColumn(SysUserRole::getUserId, userId);
        if (relations == null || relations.isEmpty()) {
            return List.of();
        }

        Set<Long> roleIds = relations.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
}

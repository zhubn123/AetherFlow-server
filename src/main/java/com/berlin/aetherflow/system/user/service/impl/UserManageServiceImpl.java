package com.berlin.aetherflow.system.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.config.ServletUtils;
import com.berlin.aetherflow.exception.ApiException;
import com.berlin.aetherflow.system.user.constant.UserConstants;
import com.berlin.aetherflow.system.user.domain.bo.UserManageUpdateBo;
import com.berlin.aetherflow.system.user.domain.entity.SysRole;
import com.berlin.aetherflow.system.user.domain.entity.SysPermission;
import com.berlin.aetherflow.system.user.domain.entity.SysUser;
import com.berlin.aetherflow.system.user.domain.entity.SysUserRole;
import com.berlin.aetherflow.system.user.domain.query.UserManageQuery;
import com.berlin.aetherflow.system.user.domain.vo.UserManageVo;
import com.berlin.aetherflow.system.user.mapper.SysRoleMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserRoleMapper;
import com.berlin.aetherflow.system.user.service.SecurityAuditService;
import com.berlin.aetherflow.system.user.service.UserManageService;
import com.berlin.aetherflow.system.user.support.RolePermissionSupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {

    private static final int USER_STATUS_NORMAL = 0;

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SecurityAuditService securityAuditService;
    private final RolePermissionSupport rolePermissionSupport;

    @Override
    public PageResult<UserManageVo> queryUserPage(UserManageQuery query) {
        UserManageQuery normalized = query == null ? new UserManageQuery() : query;
        IPage<SysUser> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(normalized.getSortBy())) {
            lqw.orderByDesc(SysUser::getCreateTime);
        }

        String username = normalizeOptional(normalized.getUsername());
        if (StringUtils.isNotBlank(username)) {
            lqw.eq(SysUser::getUsername, username);
        }

        String nickname = normalizeOptional(normalized.getNickname());
        if (StringUtils.isNotBlank(nickname)) {
            lqw.like(SysUser::getNickname, nickname);
        }

        if (normalized.getStatus() != null) {
            lqw.eq(SysUser::getStatus, normalized.getStatus());
        }

        String roleKey = normalizeOptional(normalized.getRoleKey());
        if (StringUtils.isNotBlank(roleKey)) {
            Set<Long> userIds = resolveUserIdsByRoleKey(roleKey);
            if (userIds.isEmpty()) {
                return PageResult.of(page.getCurrent(), page.getSize(), 0L, 0L, List.of());
            }
            lqw.in(SysUser::getId, userIds);
        }

        IPage<SysUser> result = sysUserMapper.selectPage(page, lqw);
        List<SysUser> users = result.getRecords();
        List<Long> userIds = users.stream()
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .toList();
        UserRoleSnapshot snapshot = buildUserRoleSnapshot(userIds);

        List<UserManageVo> records = users.stream()
                .map(user -> toVo(user, snapshot))
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserManageUpdateBo bo, HttpServletRequest request) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        SysUser operator = sysUserMapper.selectById(operatorId);
        String requestUri = request == null ? null : request.getRequestURI();
        String clientIp = request == null ? null : ServletUtils.getClientIpAddress(request);
        String operatorName = operator == null ? String.valueOf(operatorId) : operator.getUsername();

        try {
            if (userId == null) {
                throw ApiException.badRequest("用户ID不能为空");
            }
            SysUser targetUser = sysUserMapper.selectById(userId);
            if (targetUser == null) {
                throw ApiException.business("用户不存在");
            }
            if (isRootAdmin(targetUser)) {
                throw ApiException.badRequest("内置管理员 admin 不允许变更");
            }

            LinkedHashSet<String> roleKeys = normalizeRoleKeys(bo == null ? null : bo.getRoleKeys());
            List<SysRole> roles = resolveActiveRoles(roleKeys);
            Integer status = bo == null ? null : bo.getStatus();
            if (status == null) {
                throw ApiException.badRequest("状态不能为空");
            }

            // 避免管理员通过后台把当前登录账号直接改成不可继续管理用户的状态。
            if (Objects.equals(operatorId, userId)) {
                if (!Objects.equals(status, USER_STATUS_NORMAL)) {
                    throw ApiException.badRequest("当前登录管理员不能停用或锁定自己");
                }
                if (!roleKeys.contains(UserConstants.ROLE_ADMIN)) {
                    throw ApiException.badRequest("当前登录管理员不能移除自己的 admin 角色");
                }
            }

            String email = normalizeOptional(bo == null ? null : bo.getEmail());
            if (StringUtils.isNotBlank(email)) {
                SysUser duplicateEmail = sysUserMapper.selectByColumn(SysUser::getEmail, email);
                if (duplicateEmail != null && !Objects.equals(duplicateEmail.getId(), userId)) {
                    throw ApiException.business("邮箱已被占用");
                }
            }

            SysUser toUpdate = new SysUser();
            toUpdate.setId(userId);
            toUpdate.setNickname(StringUtils.defaultIfBlank(normalizeOptional(bo == null ? null : bo.getNickname()),
                    targetUser.getUsername()));
            toUpdate.setEmail(email);
            toUpdate.setPhone(normalizeOptional(bo == null ? null : bo.getPhone()));
            toUpdate.setStatus(status);
            // 后台手工改状态时，统一清空锁定时间与失败次数，避免残留临时锁影响后续判断。
            toUpdate.setLoginFailCount(0);
            toUpdate.setLockUntil(null);
            sysUserMapper.updateById(toUpdate);

            replaceUserRoles(userId, roles);

            securityAuditService.record(
                    operatorId,
                    operatorName,
                    "USER_ADMIN",
                    "UPDATE_USER",
                    requestUri,
                    clientIp,
                    1,
                    "更新用户成功：" + targetUser.getUsername()
            );
        } catch (RuntimeException ex) {
            securityAuditService.record(
                    operatorId,
                    operatorName,
                    "USER_ADMIN",
                    "UPDATE_USER",
                    requestUri,
                    clientIp,
                    0,
                    ex.getMessage()
            );
            throw ex;
        }
    }

    private UserManageVo toVo(SysUser user, UserRoleSnapshot snapshot) {
        UserManageVo vo = new UserManageVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setRoles(snapshot.roleKeyMap().getOrDefault(user.getId(), List.of()));
        vo.setPermissionKeys(snapshot.permissionKeyMap().getOrDefault(user.getId(), List.of()));
        vo.setPermissionNames(snapshot.permissionNameMap().getOrDefault(user.getId(), List.of()));
        vo.setImmutable(isRootAdmin(user));
        return vo;
    }

    private Set<Long> resolveUserIdsByRoleKey(String roleKey) {
        SysRole role = sysRoleMapper.selectByColumn(SysRole::getRoleKey, roleKey);
        if (role == null) {
            return Set.of();
        }

        List<SysUserRole> relations = sysUserRoleMapper.selectListByColumn(SysUserRole::getRoleId, role.getId());
        if (relations == null || relations.isEmpty()) {
            return Set.of();
        }
        return relations.stream()
                .map(SysUserRole::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private UserRoleSnapshot buildUserRoleSnapshot(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new UserRoleSnapshot(Map.of(), Map.of(), Map.of());
        }

        LambdaQueryWrapper<SysUserRole> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.in(SysUserRole::getUserId, userIds);
        List<SysUserRole> relations = sysUserRoleMapper.selectList(relationQuery);
        if (relations == null || relations.isEmpty()) {
            return new UserRoleSnapshot(Map.of(), Map.of(), Map.of());
        }

        Set<Long> roleIds = relations.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return new UserRoleSnapshot(Map.of(), Map.of(), Map.of());
        }

        Map<Long, SysRole> roleMap = sysRoleMapper.selectByIds(roleIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        SysRole::getId,
                        role -> role,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Map<Long, LinkedHashSet<String>> roleKeyGrouped = new LinkedHashMap<>();
        Map<Long, LinkedHashSet<Long>> roleIdGrouped = new LinkedHashMap<>();
        for (SysUserRole relation : relations) {
            SysRole role = roleMap.get(relation.getRoleId());
            if (role == null || relation.getUserId() == null) {
                continue;
            }
            if (StringUtils.isNotBlank(role.getRoleKey())) {
                roleKeyGrouped.computeIfAbsent(relation.getUserId(), key -> new LinkedHashSet<>()).add(role.getRoleKey());
            }
            if (Objects.equals(role.getStatus(), USER_STATUS_NORMAL)) {
                roleIdGrouped.computeIfAbsent(relation.getUserId(), key -> new LinkedHashSet<>()).add(role.getId());
            }
        }

        Set<Long> activeRoleIds = roleIdGrouped.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<SysPermission>> permissionMapByRoleId = rolePermissionSupport.buildActivePermissionMapByRoleIds(activeRoleIds);

        Map<Long, List<String>> roleKeyMap = new LinkedHashMap<>();
        roleKeyGrouped.forEach((userId, keys) -> roleKeyMap.put(userId, List.copyOf(keys)));

        Map<Long, List<String>> permissionKeyMap = new LinkedHashMap<>();
        Map<Long, List<String>> permissionNameMap = new LinkedHashMap<>();
        roleIdGrouped.forEach((userId, assignedRoleIds) -> {
            LinkedHashSet<String> permissionKeys = new LinkedHashSet<>();
            LinkedHashSet<String> permissionNames = new LinkedHashSet<>();
            for (Long roleId : assignedRoleIds) {
                List<SysPermission> permissions = permissionMapByRoleId.getOrDefault(roleId, List.of());
                for (SysPermission permission : permissions) {
                    if (StringUtils.isNotBlank(permission.getPermKey())) {
                        permissionKeys.add(permission.getPermKey());
                    }
                    if (StringUtils.isNotBlank(permission.getPermName())) {
                        permissionNames.add(permission.getPermName());
                    }
                }
            }
            permissionKeyMap.put(userId, List.copyOf(permissionKeys));
            permissionNameMap.put(userId, List.copyOf(permissionNames));
        });

        return new UserRoleSnapshot(roleKeyMap, permissionKeyMap, permissionNameMap);
    }

    private LinkedHashSet<String> normalizeRoleKeys(List<String> roleKeys) {
        if (roleKeys == null || roleKeys.isEmpty()) {
            throw ApiException.badRequest("至少选择一个角色");
        }

        LinkedHashSet<String> normalized = roleKeys.stream()
                .map(this::normalizeOptional)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            throw ApiException.badRequest("至少选择一个角色");
        }
        return normalized;
    }

    private List<SysRole> resolveActiveRoles(LinkedHashSet<String> roleKeys) {
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.in(SysRole::getRoleKey, roleKeys);
        List<SysRole> roles = sysRoleMapper.selectList(roleQuery).stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.equals(role.getStatus(), USER_STATUS_NORMAL))
                .toList();

        Set<String> existingKeys = roles.stream()
                .map(SysRole::getRoleKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (existingKeys.size() != roleKeys.size()) {
            LinkedHashSet<String> missingKeys = new LinkedHashSet<>(roleKeys);
            missingKeys.removeAll(existingKeys);
            throw ApiException.business("角色不存在或已停用：" + String.join("、", missingKeys));
        }
        return roles;
    }

    private void replaceUserRoles(Long userId, List<SysRole> roles) {
        LambdaQueryWrapper<SysUserRole> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(deleteQuery);

        for (SysRole role : roles) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(role.getId());
            sysUserRoleMapper.insert(relation);
        }
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }

    /**
     * 内置管理员账号作为兜底超管保留，不允许通过后台用户管理接口改资料、状态或角色。
     */
    private boolean isRootAdmin(SysUser user) {
        if (user == null) {
            return false;
        }
        return Objects.equals(user.getId(), UserConstants.ROOT_ADMIN_USER_ID)
                || Objects.equals(user.getUsername(), UserConstants.ROOT_ADMIN_USERNAME);
    }

    private record UserRoleSnapshot(
            Map<Long, List<String>> roleKeyMap,
            Map<Long, List<String>> permissionKeyMap,
            Map<Long, List<String>> permissionNameMap
    ) {
    }
}

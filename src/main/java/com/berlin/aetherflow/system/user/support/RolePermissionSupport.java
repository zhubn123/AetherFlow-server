package com.berlin.aetherflow.system.user.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.berlin.aetherflow.system.user.domain.entity.SysPermission;
import com.berlin.aetherflow.system.user.domain.entity.SysRolePermission;
import com.berlin.aetherflow.system.user.mapper.SysPermissionMapper;
import com.berlin.aetherflow.system.user.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色权限解析支持类。
 */
@Component
@RequiredArgsConstructor
public class RolePermissionSupport {

    private static final int STATUS_NORMAL = 0;

    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 按角色 ID 集合构建“角色 -> 生效权限”映射。
     */
    public Map<Long, List<SysPermission>> buildActivePermissionMapByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }

        LambdaQueryWrapper<SysRolePermission> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.in(SysRolePermission::getRoleId, roleIds);
        List<SysRolePermission> relations = sysRolePermissionMapper.selectList(relationQuery);
        if (relations == null || relations.isEmpty()) {
            return Map.of();
        }

        Set<Long> permissionIds = relations.stream()
                .map(SysRolePermission::getPermissionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (permissionIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, SysPermission> permissionMap = sysPermissionMapper.selectByIds(permissionIds).stream()
                .filter(Objects::nonNull)
                .filter(permission -> Objects.equals(permission.getStatus(), STATUS_NORMAL))
                .collect(Collectors.toMap(
                        SysPermission::getId,
                        permission -> permission,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Map<Long, LinkedHashMap<Long, SysPermission>> grouped = new LinkedHashMap<>();
        for (SysRolePermission relation : relations) {
            SysPermission permission = permissionMap.get(relation.getPermissionId());
            if (permission == null || relation.getRoleId() == null) {
                continue;
            }
            grouped.computeIfAbsent(relation.getRoleId(), key -> new LinkedHashMap<>())
                    .putIfAbsent(permission.getId(), permission);
        }

        Map<Long, List<SysPermission>> result = new LinkedHashMap<>();
        grouped.forEach((roleId, permissions) -> result.put(roleId, List.copyOf(permissions.values())));
        return result;
    }

    /**
     * 按权限标识查询启用中的权限实体。
     */
    public List<SysPermission> listActivePermissionsByKeys(Collection<String> permissionKeys) {
        if (permissionKeys == null || permissionKeys.isEmpty()) {
            return List.of();
        }

        Set<String> normalizedKeys = permissionKeys.stream()
                .map(this::normalizeOptional)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalizedKeys.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<SysPermission> permissionQuery = new LambdaQueryWrapper<>();
        permissionQuery.in(SysPermission::getPermKey, normalizedKeys);
        permissionQuery.eq(SysPermission::getStatus, STATUS_NORMAL);
        return sysPermissionMapper.selectList(permissionQuery);
    }

    /**
     * 查询全部启用权限。
     */
    public List<SysPermission> listAllActivePermissions() {
        LambdaQueryWrapper<SysPermission> permissionQuery = new LambdaQueryWrapper<>();
        permissionQuery.eq(SysPermission::getStatus, STATUS_NORMAL);
        permissionQuery.orderByAsc(SysPermission::getModule, SysPermission::getAction, SysPermission::getPermKey);
        return sysPermissionMapper.selectList(permissionQuery);
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }
}

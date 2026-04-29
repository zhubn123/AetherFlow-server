package com.berlin.aetherflow.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.exception.ApiException;
import com.berlin.aetherflow.system.user.constant.UserConstants;
import com.berlin.aetherflow.system.user.domain.bo.RoleManageSaveBo;
import com.berlin.aetherflow.system.user.domain.entity.SysPermission;
import com.berlin.aetherflow.system.user.domain.entity.SysRole;
import com.berlin.aetherflow.system.user.domain.entity.SysRolePermission;
import com.berlin.aetherflow.system.user.domain.entity.SysUserRole;
import com.berlin.aetherflow.system.user.domain.query.RoleManageQuery;
import com.berlin.aetherflow.system.user.domain.vo.RoleManageVo;
import com.berlin.aetherflow.system.user.domain.vo.RoleOptionVo;
import com.berlin.aetherflow.system.user.mapper.SysRoleMapper;
import com.berlin.aetherflow.system.user.mapper.SysRolePermissionMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserRoleMapper;
import com.berlin.aetherflow.system.user.service.RoleManageService;
import com.berlin.aetherflow.system.user.support.RolePermissionSupport;
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
 * 管理端角色服务实现。
 */
@Service
@RequiredArgsConstructor
public class RoleManageServiceImpl implements RoleManageService {

    private static final int STATUS_NORMAL = 0;
    private static final Set<String> BUILT_IN_ROLE_KEYS = Set.of(
            UserConstants.ROLE_ADMIN,
            UserConstants.ROLE_OPERATOR,
            UserConstants.ROLE_VIEWER
    );

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final RolePermissionSupport rolePermissionSupport;

    @Override
    public PageResult<RoleManageVo> queryRolePage(RoleManageQuery query) {
        RoleManageQuery normalized = query == null ? new RoleManageQuery() : query;
        IPage<SysRole> page = new Page<>(normalized.getPageNo(), normalized.getPageSize());
        OrderUtil.addOrder(page, normalized.getSortBy(), normalized.getIsAsc());

        LambdaQueryWrapper<SysRole> lqw = new LambdaQueryWrapper<>();
        if (StringUtils.isBlank(normalized.getSortBy())) {
            lqw.orderByAsc(SysRole::getRoleKey);
        }

        String roleKey = normalizeOptional(normalized.getRoleKey());
        if (StringUtils.isNotBlank(roleKey)) {
            lqw.eq(SysRole::getRoleKey, roleKey);
        }

        String roleName = normalizeOptional(normalized.getRoleName());
        if (StringUtils.isNotBlank(roleName)) {
            lqw.like(SysRole::getRoleName, roleName);
        }

        if (normalized.getStatus() != null) {
            lqw.eq(SysRole::getStatus, normalized.getStatus());
        }

        IPage<SysRole> result = sysRoleMapper.selectPage(page, lqw);
        List<SysRole> roles = result.getRecords();
        RoleSnapshot snapshot = buildRoleSnapshot(roles);

        List<RoleManageVo> records = roles.stream()
                .map(role -> toRoleManageVo(role, snapshot))
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public RoleManageVo getRoleDetail(Long roleId) {
        if (roleId == null) {
            throw ApiException.badRequest("角色ID不能为空");
        }
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw ApiException.business("角色不存在");
        }
        RoleSnapshot snapshot = buildRoleSnapshot(List.of(role));
        return toRoleManageVo(role, snapshot);
    }

    @Override
    public List<RoleOptionVo> listRoleOptions() {
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.orderByAsc(SysRole::getRoleKey);
        List<SysRole> roles = sysRoleMapper.selectList(roleQuery);
        RoleSnapshot snapshot = buildRoleSnapshot(roles);
        return roles.stream()
                .map(role -> toRoleOptionVo(role, snapshot))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleManageSaveBo bo) {
        String roleKey = normalizeRequired(bo == null ? null : bo.getRoleKey(), "角色标识不能为空");
        if (sysRoleMapper.selectByColumn(SysRole::getRoleKey, roleKey) != null) {
            throw ApiException.business("角色标识已存在");
        }

        List<SysPermission> permissions = resolvePermissionsForSave(bo.getPermissionKeys());

        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        role.setRoleName(normalizeRequired(bo.getRoleName(), "角色名称不能为空"));
        role.setStatus(bo.getStatus());
        role.setRemark(StringUtils.defaultString(normalizeOptional(bo.getRemark())));
        sysRoleMapper.insert(role);

        replaceRolePermissions(role.getId(), permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleManageSaveBo bo) {
        if (roleId == null) {
            throw ApiException.badRequest("角色ID不能为空");
        }
        SysRole existing = sysRoleMapper.selectById(roleId);
        if (existing == null) {
            throw ApiException.business("角色不存在");
        }

        boolean adminRole = isAdminRole(existing);
        if (adminRole) {
            throw ApiException.badRequest("内置 admin 角色权限固定，不支持修改");
        }

        String roleKey = normalizeRequired(bo == null ? null : bo.getRoleKey(), "角色标识不能为空");
        if (isBuiltInRole(existing) && !Objects.equals(existing.getRoleKey(), roleKey)) {
            throw ApiException.badRequest("内置角色不允许修改角色标识");
        }

        SysRole duplicate = sysRoleMapper.selectByColumn(SysRole::getRoleKey, roleKey);
        if (duplicate != null && !Objects.equals(duplicate.getId(), roleId)) {
            throw ApiException.business("角色标识已存在");
        }

        List<SysPermission> permissions = resolvePermissionsForSave(bo.getPermissionKeys());

        SysRole role = new SysRole();
        role.setId(roleId);
        role.setRoleKey(roleKey);
        role.setRoleName(normalizeRequired(bo.getRoleName(), "角色名称不能为空"));
        role.setStatus(bo.getStatus());
        role.setRemark(StringUtils.defaultString(normalizeOptional(bo.getRemark())));
        sysRoleMapper.updateById(role);

        replaceRolePermissions(roleId, permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoles(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的角色");
        }

        List<SysRole> roles = sysRoleMapper.selectByIds(ids).stream()
                .filter(Objects::nonNull)
                .toList();
        if (roles.size() != new LinkedHashSet<>(ids).size()) {
            throw ApiException.business("存在角色已被删除或不存在");
        }

        List<String> builtInRoles = roles.stream()
                .filter(this::isBuiltInRole)
                .map(SysRole::getRoleKey)
                .toList();
        if (!builtInRoles.isEmpty()) {
            throw ApiException.badRequest("内置角色不允许删除：" + String.join("、", builtInRoles));
        }

        LambdaQueryWrapper<SysUserRole> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.in(SysUserRole::getRoleId, ids);
        List<SysUserRole> relations = sysUserRoleMapper.selectList(relationQuery);
        if (relations != null && !relations.isEmpty()) {
            Set<Long> usedRoleIds = relations.stream()
                    .map(SysUserRole::getRoleId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            String usedRoleKeys = roles.stream()
                    .filter(role -> usedRoleIds.contains(role.getId()))
                    .map(SysRole::getRoleKey)
                    .collect(Collectors.joining("、"));
            throw ApiException.badRequest("存在用户仍在使用这些角色，不能删除：" + usedRoleKeys);
        }

        LambdaQueryWrapper<SysRolePermission> rolePermissionDelete = new LambdaQueryWrapper<>();
        rolePermissionDelete.in(SysRolePermission::getRoleId, ids);
        sysRolePermissionMapper.delete(rolePermissionDelete);
        sysRoleMapper.deleteByIds(ids);
    }

    private RoleManageVo toRoleManageVo(SysRole role, RoleSnapshot snapshot) {
        RoleManageVo vo = new RoleManageVo();
        vo.setId(role.getId());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleName(role.getRoleName());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        vo.setBuiltIn(isBuiltInRole(role));
        vo.setModifiable(!isAdminRole(role));
        vo.setDeletable(!isBuiltInRole(role));
        vo.setUserCount(snapshot.userCountMap().getOrDefault(role.getId(), 0L));
        List<String> permissionKeys = snapshot.permissionKeyMap().getOrDefault(role.getId(), List.of());
        List<String> permissionNames = snapshot.permissionNameMap().getOrDefault(role.getId(), List.of());
        vo.setPermissionCount(permissionKeys.size());
        vo.setPermissionKeys(permissionKeys);
        vo.setPermissionNames(permissionNames);
        return vo;
    }

    private RoleOptionVo toRoleOptionVo(SysRole role, RoleSnapshot snapshot) {
        RoleOptionVo vo = new RoleOptionVo();
        vo.setId(role.getId());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleName(role.getRoleName());
        vo.setStatus(role.getStatus());
        vo.setBuiltIn(isBuiltInRole(role));
        vo.setModifiable(!isAdminRole(role));
        vo.setPermissionKeys(snapshot.permissionKeyMap().getOrDefault(role.getId(), List.of()));
        vo.setPermissionNames(snapshot.permissionNameMap().getOrDefault(role.getId(), List.of()));
        return vo;
    }

    private RoleSnapshot buildRoleSnapshot(Collection<SysRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return new RoleSnapshot(Map.of(), Map.of(), Map.of());
        }

        List<Long> roleIds = roles.stream()
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, List<SysPermission>> permissionMap = rolePermissionSupport.buildActivePermissionMapByRoleIds(roleIds);
        Map<Long, List<String>> permissionKeyMap = new LinkedHashMap<>();
        Map<Long, List<String>> permissionNameMap = new LinkedHashMap<>();
        permissionMap.forEach((roleId, permissions) -> {
            permissionKeyMap.put(roleId, permissions.stream()
                    .map(SysPermission::getPermKey)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .toList());
            permissionNameMap.put(roleId, permissions.stream()
                    .map(SysPermission::getPermName)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .toList());
        });

        Map<Long, Long> userCountMap = buildUserCountMap(roleIds);
        return new RoleSnapshot(permissionKeyMap, permissionNameMap, userCountMap);
    }

    private Map<Long, Long> buildUserCountMap(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }

        LambdaQueryWrapper<SysUserRole> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.in(SysUserRole::getRoleId, roleIds);
        List<SysUserRole> relations = sysUserRoleMapper.selectList(relationQuery);
        if (relations == null || relations.isEmpty()) {
            return Map.of();
        }

        return relations.stream()
                .filter(relation -> relation.getRoleId() != null)
                .collect(Collectors.groupingBy(
                        SysUserRole::getRoleId,
                        LinkedHashMap::new,
                        Collectors.mapping(SysUserRole::getUserId,
                                Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                                        set -> Long.valueOf(set.size())))
                ));
    }

    private List<SysPermission> resolvePermissionsForSave(List<String> permissionKeys) {
        LinkedHashSet<String> normalizedKeys = normalizePermissionKeys(permissionKeys);
        List<SysPermission> permissions = rolePermissionSupport.listActivePermissionsByKeys(normalizedKeys);
        Set<String> existingKeys = permissions.stream()
                .map(SysPermission::getPermKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (existingKeys.size() != normalizedKeys.size()) {
            LinkedHashSet<String> missingKeys = new LinkedHashSet<>(normalizedKeys);
            missingKeys.removeAll(existingKeys);
            throw ApiException.business("权限不存在或已停用：" + String.join("、", missingKeys));
        }
        return permissions;
    }

    private void replaceRolePermissions(Long roleId, List<SysPermission> permissions) {
        LambdaQueryWrapper<SysRolePermission> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(deleteQuery);

        for (SysPermission permission : permissions) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permission.getId());
            sysRolePermissionMapper.insert(relation);
        }
    }

    private LinkedHashSet<String> normalizePermissionKeys(List<String> permissionKeys) {
        if (permissionKeys == null || permissionKeys.isEmpty()) {
            throw ApiException.badRequest("至少选择一个权限");
        }
        LinkedHashSet<String> normalized = permissionKeys.stream()
                .map(this::normalizeOptional)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            throw ApiException.badRequest("至少选择一个权限");
        }
        return normalized;
    }

    private boolean isBuiltInRole(SysRole role) {
        return role != null && BUILT_IN_ROLE_KEYS.contains(role.getRoleKey());
    }

    private boolean isAdminRole(SysRole role) {
        return role != null && Objects.equals(role.getRoleKey(), UserConstants.ROLE_ADMIN);
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

    private record RoleSnapshot(
            Map<Long, List<String>> permissionKeyMap,
            Map<Long, List<String>> permissionNameMap,
            Map<Long, Long> userCountMap
    ) {
    }
}

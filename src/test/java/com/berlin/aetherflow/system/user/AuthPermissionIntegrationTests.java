package com.berlin.aetherflow.system.user;

import com.berlin.aetherflow.system.user.constant.PermissionConstants;
import com.berlin.aetherflow.system.user.domain.entity.SysRole;
import com.berlin.aetherflow.system.user.domain.entity.SysUser;
import com.berlin.aetherflow.system.user.domain.entity.SysUserRole;
import com.berlin.aetherflow.system.user.mapper.SysRoleMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserMapper;
import com.berlin.aetherflow.system.user.mapper.SysUserRoleMapper;
import com.berlin.aetherflow.system.user.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthPermissionIntegrationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    private final List<Long> userIds = new ArrayList<>();
    private final List<Long> userRoleIds = new ArrayList<>();

    @AfterEach
    void cleanupFixture() {
        if (!userRoleIds.isEmpty()) {
            sysUserRoleMapper.deleteByIds(userRoleIds);
            userRoleIds.clear();
        }
        if (!userIds.isEmpty()) {
            sysUserMapper.deleteByIds(userIds);
            userIds.clear();
        }
    }

    @Test
    void operatorShouldManageOrdersButNotMasterData() {
        Long userId = createUserWithRole("operator");

        Set<String> permissions = Set.copyOf(authService.getPermissionKeysByUserId(userId));

        assertTrue(permissions.contains(PermissionConstants.WMS_WAREHOUSE_VIEW));
        assertTrue(permissions.contains(PermissionConstants.WMS_OUTBOUND_ORDER_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.WMS_INVENTORY_ADJUSTMENT_MANAGE));

        assertFalse(permissions.contains(PermissionConstants.SYSTEM_USER_VIEW));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_USER_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_ROLE_VIEW));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_ROLE_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_PERMISSION_VIEW));
        assertFalse(permissions.contains(PermissionConstants.WMS_WAREHOUSE_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.WMS_AREA_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.WMS_LOCATION_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.WMS_MATERIAL_MANAGE));
    }

    @Test
    void viewerShouldOnlyHaveReadPermissions() {
        Long userId = createUserWithRole("viewer");

        Set<String> permissions = Set.copyOf(authService.getPermissionKeysByUserId(userId));

        assertTrue(permissions.contains(PermissionConstants.WMS_WAREHOUSE_VIEW));
        assertTrue(permissions.contains(PermissionConstants.WMS_OUTBOUND_ORDER_VIEW));
        assertTrue(permissions.contains(PermissionConstants.WMS_INVENTORY_VIEW));

        assertFalse(permissions.contains(PermissionConstants.SYSTEM_USER_VIEW));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_USER_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_ROLE_VIEW));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_ROLE_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.SYSTEM_PERMISSION_VIEW));
        assertFalse(permissions.contains(PermissionConstants.WMS_OUTBOUND_ORDER_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.WMS_INBOUND_ORDER_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.WMS_INVENTORY_ADJUSTMENT_MANAGE));
        assertFalse(permissions.contains(PermissionConstants.WMS_WAREHOUSE_MANAGE));
    }

    @Test
    void adminShouldHaveAllSeededPermissions() {
        Long userId = createUserWithRole("admin");

        Set<String> permissions = Set.copyOf(authService.getPermissionKeysByUserId(userId));

        assertTrue(permissions.contains(PermissionConstants.WMS_OPTION_VIEW));
        assertTrue(permissions.contains(PermissionConstants.WMS_WAREHOUSE_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.WMS_AREA_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.WMS_LOCATION_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.WMS_MATERIAL_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.WMS_OUTBOUND_ORDER_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.WMS_INVENTORY_ADJUSTMENT_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.SYSTEM_USER_VIEW));
        assertTrue(permissions.contains(PermissionConstants.SYSTEM_USER_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.SYSTEM_ROLE_VIEW));
        assertTrue(permissions.contains(PermissionConstants.SYSTEM_ROLE_MANAGE));
        assertTrue(permissions.contains(PermissionConstants.SYSTEM_PERMISSION_VIEW));
    }

    private Long createUserWithRole(String roleKey) {
        SysRole role = sysRoleMapper.selectByColumn(SysRole::getRoleKey, roleKey);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        SysUser user = new SysUser();
        user.setUsername("perm_" + roleKey + "_" + suffix);
        user.setPasswordHash("noop");
        user.setNickname("perm-" + roleKey);
        user.setStatus(0);
        user.setLoginFailCount(0);
        sysUserMapper.insert(user);
        userIds.add(user.getId());

        SysUserRole relation = new SysUserRole();
        relation.setUserId(user.getId());
        relation.setRoleId(role.getId());
        sysUserRoleMapper.insert(relation);
        userRoleIds.add(relation.getId());

        return user.getId();
    }
}

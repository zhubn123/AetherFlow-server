package com.berlin.aetherflow.system.user.service.impl;

import com.berlin.aetherflow.system.user.domain.entity.SysPermission;
import com.berlin.aetherflow.system.user.domain.vo.PermissionGroupVo;
import com.berlin.aetherflow.system.user.domain.vo.PermissionOptionVo;
import com.berlin.aetherflow.system.user.service.PermissionCatalogService;
import com.berlin.aetherflow.system.user.support.RolePermissionSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 权限目录服务实现。
 */
@Service
@RequiredArgsConstructor
public class PermissionCatalogServiceImpl implements PermissionCatalogService {

    private final RolePermissionSupport rolePermissionSupport;

    @Override
    public List<PermissionGroupVo> listPermissionCatalog() {
        List<SysPermission> permissions = rolePermissionSupport.listAllActivePermissions();
        Map<String, List<PermissionOptionVo>> grouped = new LinkedHashMap<>();
        for (SysPermission permission : permissions) {
            grouped.computeIfAbsent(permission.getModule(), key -> new java.util.ArrayList<>())
                    .add(toPermissionOption(permission));
        }

        return grouped.entrySet().stream()
                .map(entry -> {
                    PermissionGroupVo group = new PermissionGroupVo();
                    group.setModule(entry.getKey());
                    group.setModuleName(resolveModuleName(entry.getKey()));
                    group.setPermissions(List.copyOf(entry.getValue()));
                    return group;
                })
                .toList();
    }

    private PermissionOptionVo toPermissionOption(SysPermission permission) {
        PermissionOptionVo vo = new PermissionOptionVo();
        vo.setId(permission.getId());
        vo.setPermKey(permission.getPermKey());
        vo.setPermName(permission.getPermName());
        vo.setModule(permission.getModule());
        vo.setModuleName(resolveModuleName(permission.getModule()));
        vo.setAction(permission.getAction());
        vo.setStatus(permission.getStatus());
        vo.setRemark(permission.getRemark());
        return vo;
    }

    private String resolveModuleName(String module) {
        if ("system".equalsIgnoreCase(module)) {
            return "系统管理";
        }
        if ("wms".equalsIgnoreCase(module)) {
            return "WMS 业务";
        }
        return module == null ? "未分类" : module.toUpperCase(Locale.ROOT);
    }
}

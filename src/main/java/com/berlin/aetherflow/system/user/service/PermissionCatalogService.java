package com.berlin.aetherflow.system.user.service;

import com.berlin.aetherflow.system.user.domain.vo.PermissionGroupVo;

import java.util.List;

/**
 * 权限目录服务。
 */
public interface PermissionCatalogService {

    /**
     * 查询权限目录。
     *
     * @return 按模块分组的权限列表
     */
    List<PermissionGroupVo> listPermissionCatalog();
}

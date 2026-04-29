package com.berlin.aetherflow.system.user.mapper;

import com.berlin.aetherflow.common.BaseMapperPlus;
import com.berlin.aetherflow.system.user.domain.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper。
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapperPlus<SysRolePermission> {
}

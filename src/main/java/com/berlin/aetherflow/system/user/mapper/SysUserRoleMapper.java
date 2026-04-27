package com.berlin.aetherflow.system.user.mapper;

import com.berlin.aetherflow.common.BaseMapperPlus;
import com.berlin.aetherflow.system.user.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper。
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapperPlus<SysUserRole> {
}

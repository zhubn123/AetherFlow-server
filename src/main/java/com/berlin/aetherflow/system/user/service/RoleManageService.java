package com.berlin.aetherflow.system.user.service;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.system.user.domain.bo.RoleManageSaveBo;
import com.berlin.aetherflow.system.user.domain.query.RoleManageQuery;
import com.berlin.aetherflow.system.user.domain.vo.RoleManageVo;
import com.berlin.aetherflow.system.user.domain.vo.RoleOptionVo;

import java.util.List;

/**
 * 管理端角色服务。
 */
public interface RoleManageService {

    /**
     * 分页查询角色。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<RoleManageVo> queryRolePage(RoleManageQuery query);

    /**
     * 查询角色详情。
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleManageVo getRoleDetail(Long roleId);

    /**
     * 查询角色选项。
     *
     * @return 角色选项列表
     */
    List<RoleOptionVo> listRoleOptions();

    /**
     * 创建角色。
     *
     * @param bo 保存参数
     */
    void createRole(RoleManageSaveBo bo);

    /**
     * 更新角色。
     *
     * @param roleId 角色ID
     * @param bo     保存参数
     */
    void updateRole(Long roleId, RoleManageSaveBo bo);

    /**
     * 批量删除角色。
     *
     * @param ids 角色ID集合
     */
    void deleteRoles(List<Long> ids);
}

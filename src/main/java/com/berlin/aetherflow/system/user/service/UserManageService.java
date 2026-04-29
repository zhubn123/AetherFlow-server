package com.berlin.aetherflow.system.user.service;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.system.user.domain.bo.UserManageUpdateBo;
import com.berlin.aetherflow.system.user.domain.query.UserManageQuery;
import com.berlin.aetherflow.system.user.domain.vo.UserManageVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理端用户服务。
 */
public interface UserManageService {

    /**
     * 分页查询用户。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<UserManageVo> queryUserPage(UserManageQuery query);

    /**
     * 管理端更新用户资料、状态与角色。
     *
     * @param userId  用户 ID
     * @param bo      更新参数
     * @param request 请求对象
     */
    void updateUser(Long userId, UserManageUpdateBo bo, HttpServletRequest request);
}

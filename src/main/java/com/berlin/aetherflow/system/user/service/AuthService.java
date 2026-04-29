package com.berlin.aetherflow.system.user.service;

import com.berlin.aetherflow.system.user.domain.bo.AuthLoginBo;
import com.berlin.aetherflow.system.user.domain.bo.AuthRefreshBo;
import com.berlin.aetherflow.system.user.domain.bo.AuthRegisterBo;
import com.berlin.aetherflow.system.user.domain.vo.AuthLoginVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 认证与授权服务。
 */
public interface AuthService {

    /**
     * 用户注册。
     *
     * @param bo 注册参数
     * @return 用户 ID
     */
    Long register(AuthRegisterBo bo);

    /**
     * 用户登录。
     *
     * @param bo      登录参数
     * @param request 请求对象
     * @return 登录响应
     */
    AuthLoginVo login(AuthLoginBo bo, HttpServletRequest request);

    /**
     * 刷新登录令牌。
     *
     * @param bo      刷新参数
     * @param request 请求对象
     * @return 新登录响应
     */
    AuthLoginVo refresh(AuthRefreshBo bo, HttpServletRequest request);

    /**
     * 用户登出。
     */
    void logout();

    /**
     * 按用户 ID 查询角色 key 列表。
     *
     * @param userId 用户 ID
     * @return 角色 key 列表
     */
    List<String> getRoleKeysByUserId(Long userId);

    /**
     * 按用户 ID 查询权限 key 列表。
     *
     * @param userId 用户 ID
     * @return 权限 key 列表
     */
    List<String> getPermissionKeysByUserId(Long userId);
}

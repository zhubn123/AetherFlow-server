package com.berlin.aetherflow.system.user.service;

import com.berlin.aetherflow.system.user.domain.bo.UserPasswordUpdateBo;
import com.berlin.aetherflow.system.user.domain.bo.UserProfileUpdateBo;
import com.berlin.aetherflow.system.user.domain.vo.UserProfileVo;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户个人资料服务。
 */
public interface UserProfileService {

    /**
     * 获取当前登录用户资料。
     *
     * @return 个人资料
     */
    UserProfileVo getCurrentProfile();

    /**
     * 更新当前登录用户资料。
     *
     * @param bo      更新参数
     * @param request 请求对象
     */
    void updateCurrentProfile(UserProfileUpdateBo bo, HttpServletRequest request);

    /**
     * 修改当前登录用户密码。
     *
     * @param bo      修改密码参数
     * @param request 请求对象
     */
    void updateCurrentPassword(UserPasswordUpdateBo bo, HttpServletRequest request);
}

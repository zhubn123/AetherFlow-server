package com.berlin.aetherflow.modules.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.modules.system.user.domain.bo.LoginUser;
import com.berlin.aetherflow.modules.system.user.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {
    String login(LoginUser user, HttpServletRequest request);
}

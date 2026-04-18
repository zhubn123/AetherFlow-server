package com.berlin.aetherflow.modules.system.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.config.ServletUtils;
import com.berlin.aetherflow.modules.system.user.domain.bo.LoginUser;
import com.berlin.aetherflow.modules.system.user.domain.entity.User;
import com.berlin.aetherflow.modules.system.user.mapper.UserMapper;
import com.berlin.aetherflow.modules.system.user.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    // 本地缓存实例 TODO 只用于单节点部署，集群环境需修改
    private final Cache<String, Integer> loginErrorCache = Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();


    @Override
    public String login(LoginUser loginUser, HttpServletRequest request) {
        User user = userMapper.selectByColumn(User::getUsername, loginUser.getUsername());

        if (user == null || user.getPassword() == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        String ip = ServletUtils.getClientIpAddress(request);
        // checkLogin(user.getUsername(), ip,() -> !user.getPassword().equals(loginUser.getPassword()));
        checkLogin(user.getUsername(), ip,() -> !BCrypt.checkpw(loginUser.getPassword(), user.getPassword()));

        return StpUtil.getTokenValue();
    }

    /**
     * 登录校验
     */
    private void checkLogin(String username, String clientIP, Supplier<Boolean> supplier) {
        String errorKey = "pwd_err_cnt:" + username + ":" + clientIP;

        // 获取用户登录错误次数，默认为0
        Integer errorNumber = loginErrorCache.getIfPresent(errorKey);
        if (errorNumber == null) {
            errorNumber = 0;
        }

        // 锁定时间内登录 则踢出
        if (errorNumber >= 5) { // 使用默认的最大重试次数
            System.out.println("登录失败，错误次数超限: " + username + ", 最大尝试次数: 5");
            throw new RuntimeException("登录失败，错误次数超限，请5分钟后重试");
        }

        if (supplier.get()) {
            // 错误次数递增
            errorNumber++;
            loginErrorCache.put(errorKey, errorNumber);

            // 达到规定错误次数 则锁定登录
            if (errorNumber >= 5) { // 使用默认的最大重试次数
                System.out.println("登录失败，错误次数超限: " + username + ", 最大尝试次数: 5");
                throw new RuntimeException("登录失败，错误次数超限，请5分钟后重试");
            } else {
                // 未达到规定错误次数
                System.out.println("登录失败，错误次数: " + errorNumber);
                throw new RuntimeException("登录失败，错误次数，请5分钟后重试: " + errorNumber);
            }
        }

        // 登录成功 清空错误次数
        loginErrorCache.invalidate(errorKey);
        System.out.println("登录成功: " + username);
    }
}

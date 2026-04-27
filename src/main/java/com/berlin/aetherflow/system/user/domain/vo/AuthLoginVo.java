package com.berlin.aetherflow.system.user.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录返回体。
 */
@Data
public class AuthLoginVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录令牌
     */
    private String token;

    /**
     * 用户信息
     */
    private AuthUserInfoVo userInfo;

    /**
     * 角色列表（role_key）
     */
    private List<String> roles;
}

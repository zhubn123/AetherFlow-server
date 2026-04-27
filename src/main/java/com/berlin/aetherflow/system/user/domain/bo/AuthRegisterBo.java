package com.berlin.aetherflow.system.user.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 注册参数。
 */
@Data
public class AuthRegisterBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（明文，仅用于注册时接收并加密存储）
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;
}

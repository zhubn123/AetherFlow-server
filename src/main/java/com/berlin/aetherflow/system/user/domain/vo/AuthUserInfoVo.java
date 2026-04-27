package com.berlin.aetherflow.system.user.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录返回中的用户信息。
 */
@Data
public class AuthUserInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;
}

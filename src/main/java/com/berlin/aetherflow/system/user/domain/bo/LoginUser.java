package com.berlin.aetherflow.system.user.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * LoginUser
 *
 * @author zhubn
 * @date 2026/4/18
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}

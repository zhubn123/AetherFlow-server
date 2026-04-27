package com.berlin.aetherflow.system.user.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改密码参数。
 */
@Data
public class UserPasswordUpdateBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
}

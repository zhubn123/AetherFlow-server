package com.berlin.aetherflow.system.user.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 刷新令牌请求体。
 */
@Data
public class AuthRefreshBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 刷新令牌。
     */
    private String refreshToken;
}

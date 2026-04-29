package com.berlin.aetherflow.system.user.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 管理端用户分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserManageQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名（精确匹配）
     */
    @Schema(description = "用户名（精确匹配）")
    @Size(max = 64, message = "用户名长度不能超过64位")
    private String username;

    /**
     * 昵称（模糊匹配）
     */
    @Schema(description = "昵称（模糊匹配）")
    @Size(max = 64, message = "昵称长度不能超过64位")
    private String nickname;

    /**
     * 角色标识
     */
    @Schema(description = "角色标识")
    @Size(max = 64, message = "角色标识长度不能超过64位")
    private String roleKey;

    /**
     * 状态（0正常 1停用 2锁定）
     */
    @Schema(description = "状态（0正常 1停用 2锁定）")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 2, message = "状态值非法")
    private Integer status;
}

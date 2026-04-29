package com.berlin.aetherflow.system.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端用户更新参数。
 */
@Data
public class UserManageUpdateBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    @Size(max = 64, message = "昵称长度不能超过64位")
    private String nickname;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128位")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    @Size(max = 32, message = "手机号长度不能超过32位")
    private String phone;

    /**
     * 状态（0正常 1停用 2锁定）
     */
    @Schema(description = "状态（0正常 1停用 2锁定）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 2, message = "状态值非法")
    private Integer status;

    /**
     * 角色标识集合
     */
    @Schema(description = "角色标识集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "至少选择一个角色")
    @Size(max = 8, message = "角色数量不能超过8个")
    private List<@NotNull(message = "角色标识不能为空") @Size(min = 1, max = 64, message = "角色标识长度非法") String> roleKeys;
}

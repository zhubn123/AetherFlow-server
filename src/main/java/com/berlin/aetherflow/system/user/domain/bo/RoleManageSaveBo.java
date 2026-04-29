package com.berlin.aetherflow.system.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端角色保存参数。
 */
@Data
public class RoleManageSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色标识
     */
    @Schema(description = "角色标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色标识不能为空")
    @Size(max = 64, message = "角色标识长度不能超过64位")
    @Pattern(regexp = "^[a-z][a-z0-9:_-]{1,63}$", message = "角色标识必须以小写字母开头，且仅支持小写字母、数字、:、_、-")
    private String roleKey;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过64位")
    private String roleName;

    /**
     * 状态（0正常 1停用）
     */
    @Schema(description = "状态（0正常 1停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255位")
    private String remark;

    /**
     * 权限标识集合
     */
    @Schema(description = "权限标识集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "至少选择一个权限")
    @Size(max = 128, message = "权限数量不能超过128个")
    private List<@NotBlank(message = "权限标识不能为空") @Size(max = 128, message = "权限标识长度不能超过128位") String> permissionKeys;
}

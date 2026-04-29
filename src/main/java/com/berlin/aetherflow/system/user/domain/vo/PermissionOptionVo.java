package com.berlin.aetherflow.system.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限选项对象。
 */
@Data
public class PermissionOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限标识")
    private String permKey;

    @Schema(description = "权限名称")
    private String permName;

    @Schema(description = "所属模块")
    private String module;

    @Schema(description = "所属模块名称")
    private String moduleName;

    @Schema(description = "动作标识")
    private String action;

    @Schema(description = "状态（0正常 1停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}

package com.berlin.aetherflow.system.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 按模块分组的权限目录。
 */
@Data
public class PermissionGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "模块编码")
    private String module;

    @Schema(description = "模块名称")
    private String moduleName;

    @Schema(description = "权限列表")
    private List<PermissionOptionVo> permissions;
}

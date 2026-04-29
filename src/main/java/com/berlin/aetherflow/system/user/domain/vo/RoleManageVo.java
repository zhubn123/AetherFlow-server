package com.berlin.aetherflow.system.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 管理端角色返回对象。
 */
@Data
public class RoleManageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色标识")
    private String roleKey;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "状态（0正常 1停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否内置角色")
    private Boolean builtIn;

    @Schema(description = "是否允许修改")
    private Boolean modifiable;

    @Schema(description = "是否允许删除")
    private Boolean deletable;

    @Schema(description = "关联用户数量")
    private Long userCount;

    @Schema(description = "权限数量")
    private Integer permissionCount;

    @Schema(description = "权限标识集合")
    private List<String> permissionKeys;

    @Schema(description = "权限名称集合")
    private List<String> permissionNames;
}

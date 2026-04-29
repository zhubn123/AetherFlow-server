package com.berlin.aetherflow.system.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统权限实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 权限标识（如 wms:warehouse:view）
     */
    private String permKey;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 动作标识（view/manage）
     */
    private String action;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

package com.berlin.aetherflow.wms.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 区域实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("area")
public class Area extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域编码。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String areaCode;

    /**
     * 区域名称。
     */
    private String areaName;

    /**
     * 区域类型。
     */
    private String areaType;

    /**
     * 状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}

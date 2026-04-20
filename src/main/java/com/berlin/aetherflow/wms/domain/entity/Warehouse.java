package com.berlin.aetherflow.wms.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 仓库实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("warehouse")
public class Warehouse extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 仓库编码。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String warehouseCode;

    /**
     * 仓库名称。
     */
    private String warehouseName;

    /**
     * 仓库状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}

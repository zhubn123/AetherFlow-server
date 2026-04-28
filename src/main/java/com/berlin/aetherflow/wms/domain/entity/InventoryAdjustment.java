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
import java.time.LocalDateTime;

/**
 * 库存调整单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_adjustment")
public class InventoryAdjustment extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 调整单号。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String orderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 调整方向（INCREASE/DECREASE）。
     */
    private String adjustType;

    /**
     * 状态（0草稿 1已确认）。
     */
    private Integer status;

    /**
     * 实际调整时间。
     */
    private LocalDateTime adjustTime;

    /**
     * 调整原因。
     */
    private String adjustReason;

    /**
     * 备注。
     */
    private String remark;
}

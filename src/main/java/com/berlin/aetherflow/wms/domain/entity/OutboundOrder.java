package com.berlin.aetherflow.wms.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order")
public class OutboundOrder extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 出库单号。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String orderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 状态（0草稿 1已确认）。
     */
    private Integer status;

    /**
     * 总数量。
     */
    private BigDecimal totalQty;

    /**
     * 出库时间。
     */
    private LocalDateTime outboundTime;

    /**
     * 备注。
     */
    private String remark;
}

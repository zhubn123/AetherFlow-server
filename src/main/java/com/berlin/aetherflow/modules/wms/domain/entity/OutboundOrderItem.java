package com.berlin.aetherflow.modules.wms.outbound.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 出库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order_item")
public class OutboundOrderItem extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 出库单ID。
     */
    private Long orderId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 出库数量。
     */
    private BigDecimal qty;

    /**
     * 备注。
     */
    private String remark;
}

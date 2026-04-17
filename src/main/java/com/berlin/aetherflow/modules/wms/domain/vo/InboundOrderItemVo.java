package com.berlin.aetherflow.modules.wms.domain.vo;

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
 * 入库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_order_item")
public class InboundOrderItemVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 入库单ID。
     */
    private Long orderId;

    /**
     * 行号。
     */
    private Integer lineNo;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 目标库位ID。
     */
    private Long locationId;

    /**
     * 计划入库数量。
     */
    private BigDecimal plannedQty;

    /**
     * 已入库数量。
     */
    private BigDecimal receivedQty;

    /**
     * 备注。
     */
    private String remark;
}

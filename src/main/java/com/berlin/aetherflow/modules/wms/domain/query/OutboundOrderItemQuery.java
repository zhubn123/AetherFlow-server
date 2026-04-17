package com.berlin.aetherflow.modules.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.modules.wms.domain.entity.OutboundOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 出库单明细实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrderItem.class, reverseConvertGenerate = false)
public class OutboundOrderItemQuery extends PageQuery {

    private Long id;

    /**
     * 出库单ID。
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
     * 来源库位ID。
     */
    private Long locationId;

    /**
     * 计划出库数量。
     */
    private BigDecimal plannedQty;

    /**
     * 已出库数量。
     */
    private BigDecimal shippedQty;

    /**
     * 备注。
     */
    private String remark;
}

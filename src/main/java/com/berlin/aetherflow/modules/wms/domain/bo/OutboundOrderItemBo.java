package com.berlin.aetherflow.modules.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.modules.wms.domain.entity.OutboundOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 出库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrderItem.class, reverseConvertGenerate = false)
public class OutboundOrderItemBo extends BaseEntity {

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

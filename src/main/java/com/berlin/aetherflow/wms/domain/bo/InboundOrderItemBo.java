package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.InboundOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 入库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InboundOrderItem.class, reverseConvertGenerate = false)
public class InboundOrderItemBo extends BaseEntity {

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

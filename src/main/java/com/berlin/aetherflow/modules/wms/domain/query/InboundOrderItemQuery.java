package com.berlin.aetherflow.modules.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.modules.wms.domain.entity.InboundOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 入库单明细实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InboundOrderItem.class, reverseConvertGenerate = false)
public class InboundOrderItemQuery extends PageQuery {

    private Long id;

    /**
     * 入库单ID。
     */
    private Long orderId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 入库数量。
     */
    private BigDecimal qty;

    /**
     * 备注。
     */
    private String remark;
}

package com.berlin.aetherflow.modules.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.modules.wms.domain.entity.InboundOrderItem;
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

package com.berlin.aetherflow.modules.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.modules.wms.domain.entity.InboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InboundOrder.class, reverseConvertGenerate = false)
public class InboundOrderBo extends BaseEntity {

    private Long id;

    /**
     * 入库单号。
     */
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
     * 入库时间。
     */
    private LocalDateTime inboundTime;

    /**
     * 备注。
     */
    private String remark;
}

package com.berlin.aetherflow.modules.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.modules.wms.domain.entity.InboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InboundOrder.class, reverseConvertGenerate = false)
public class InboundOrderQuery extends PageQuery {

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
     * 高于数量
     */
    private BigDecimal minQty;
    /**
     * 低于数量
     */
    private BigDecimal maxQty;

    /**
     * 入库时间起始。
     */
    private LocalDateTime inboundStartTime;

    /**
     * 入库时间结束。
     */
    private LocalDateTime inboundEndTime;

    /**
     * 备注。
     */
    private String remark;
}

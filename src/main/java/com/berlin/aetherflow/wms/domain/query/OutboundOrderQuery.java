package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 出库单实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrder.class, reverseConvertGenerate = false)
public class OutboundOrderQuery extends PageQuery {

    /**
     * 出库单号。
     */
    private String orderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 状态（0草稿 1已确认）。
     */
    private Integer status;

    /**
     * 实际出库时间。
     */
    private LocalDateTime outboundTime;

    /**
     * 备注。
     */
    private String remark;
}

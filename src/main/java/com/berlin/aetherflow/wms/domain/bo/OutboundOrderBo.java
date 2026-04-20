package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrder.class, reverseConvertGenerate = false)
public class OutboundOrderBo extends BaseEntity {

    private Long id;

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

    /**
     * 出库单明细。
     */
    private List<OutboundOrderItemBo> orderItemsBo;
}

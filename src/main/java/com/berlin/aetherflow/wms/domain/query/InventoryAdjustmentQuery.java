package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 库存调整单查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InventoryAdjustment.class, reverseConvertGenerate = false)
public class InventoryAdjustmentQuery extends PageQuery {

    /**
     * 调整单号。
     */
    private String orderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 调整方向。
     */
    private String adjustType;

    /**
     * 状态。
     */
    private Integer status;

    /**
     * 调整时间起始。
     */
    private LocalDateTime adjustStartTime;

    /**
     * 调整时间结束。
     */
    private LocalDateTime adjustEndTime;

    /**
     * 调整原因。
     */
    private String adjustReason;

    /**
     * 备注。
     */
    private String remark;
}

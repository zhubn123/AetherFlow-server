package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.StockTransaction;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 库存流水查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = StockTransaction.class, reverseConvertGenerate = false)
public class StockTransactionQuery extends PageQuery {

    private Long id;

    /**
     * 业务类型。
     */
    private String bizType;

    /**
     * 业务单据ID。
     */
    private Long bizId;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 操作人ID。
     */
    private Long operatorId;

    /**
     * 操作时间起始。
     */
    private LocalDateTime operateStartTime;

    /**
     * 操作时间结束。
     */
    private LocalDateTime operateEndTime;

    /**
     * 备注。
     */
    private String remark;
}

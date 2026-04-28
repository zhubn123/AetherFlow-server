package com.berlin.aetherflow.wms.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一库存变动入口参数。
 */
@Data
public class StockChangeBo {

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
     * 库位ID。
     */
    private Long locationId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 变动数量（正数入库，负数出库）。
     */
    private BigDecimal changeQty;

    /**
     * 操作时间。
     */
    private LocalDateTime operateTime;

    /**
     * 备注。
     */
    private String remark;
}

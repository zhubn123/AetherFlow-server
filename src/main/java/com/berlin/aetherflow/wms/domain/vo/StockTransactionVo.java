package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.StockTransaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水返回对象。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = StockTransaction.class, convertGenerate = false)
public class StockTransactionVo extends BaseEntity {

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
     * 仓库编码。
     */
    private String warehouseCode;

    /**
     * 仓库名称。
     */
    private String warehouseName;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 区域编码。
     */
    private String areaCode;

    /**
     * 区域名称。
     */
    private String areaName;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 库位编码。
     */
    private String locationCode;

    /**
     * 库位名称。
     */
    private String locationName;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 物料编码。
     */
    private String materialCode;

    /**
     * 物料名称。
     */
    private String materialName;

    /**
     * 变动数量。
     */
    private BigDecimal changeQty;

    /**
     * 变动前库存。
     */
    private BigDecimal beforeQty;

    /**
     * 变动后库存。
     */
    private BigDecimal afterQty;

    /**
     * 操作人ID。
     */
    private Long operatorId;

    /**
     * 操作人名称。
     */
    private String operatorName;

    /**
     * 操作时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operateTime;

    /**
     * 备注。
     */
    private String remark;
}

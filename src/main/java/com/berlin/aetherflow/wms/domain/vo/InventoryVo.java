package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 库存实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Inventory.class, convertGenerate = false)
public class InventoryVo extends BaseEntity {

    private Long id;

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
     * 库位ID。
     */
    private Long locationId;

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
     * 当前库存。
     */
    private BigDecimal quantity;

    /**
     * 锁定库存。
     */
    private BigDecimal lockedQuantity;
}

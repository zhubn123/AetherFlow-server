package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Inventory.class, reverseConvertGenerate = false)
public class InventoryQuery extends PageQuery {

    private Long id;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 当前库存最小值。
     */
    private BigDecimal minQuantity;

    /**
     * 当前库存最小值。
     */
    private BigDecimal maxQuantity;

    /**
     * 锁定库存。
     */
    private BigDecimal lockedQuantity;
}

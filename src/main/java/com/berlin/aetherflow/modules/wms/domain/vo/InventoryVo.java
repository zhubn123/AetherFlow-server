package com.berlin.aetherflow.modules.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.modules.wms.domain.entity.Inventory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Inventory.class, convertGenerate = false)
public class InventoryVo extends BaseEntity {

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
     * 物料ID。
     */
    private Long materialId;

    /**
     * 当前库存。
     */
    private BigDecimal quantity;

    /**
     * 锁定库存。
     */
    private BigDecimal lockedQuantity;
}

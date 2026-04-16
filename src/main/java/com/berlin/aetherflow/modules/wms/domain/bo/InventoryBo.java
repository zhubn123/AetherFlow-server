package com.berlin.aetherflow.modules.wms.domain.bo;

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
@AutoMapper(target = Inventory.class, reverseConvertGenerate = false)
public class InventoryBo extends BaseEntity {

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
     * 当前库存最小值。
     */
    private BigDecimal minQuantity;

    /**
     * 当前库存最小值。
     */
    private BigDecimal maxQuantity;

    /**
     * 锁定库存。TODO 只做修改
     */
    private BigDecimal lockedQuantity;
}

package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 库存调整单详情返回对象。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = InventoryAdjustment.class, convertGenerate = false)
public class InventoryAdjustmentDetailVo extends InventoryAdjustmentVo {

    /**
     * 调整明细。
     */
    private List<InventoryAdjustmentItemVo> adjustmentItems;
}

package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentItemBo;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustmentItem;

import java.util.List;

/**
 * 库存调整单明细 Service。
 */
public interface InventoryAdjustmentItemService extends IService<InventoryAdjustmentItem> {

    void saveInventoryAdjustmentItems(List<InventoryAdjustmentItemBo> itemsBo);

    void replaceInventoryAdjustmentItems(Long orderId, List<InventoryAdjustmentItemBo> itemsBo);
}

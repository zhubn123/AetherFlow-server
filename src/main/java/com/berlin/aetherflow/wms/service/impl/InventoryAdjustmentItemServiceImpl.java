package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentItemBo;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustmentItem;
import com.berlin.aetherflow.wms.mapper.InventoryAdjustmentItemMapper;
import com.berlin.aetherflow.wms.service.InventoryAdjustmentItemService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 库存调整单明细 Service 实现。
 */
@Service
public class InventoryAdjustmentItemServiceImpl extends ServiceImpl<InventoryAdjustmentItemMapper, InventoryAdjustmentItem>
        implements InventoryAdjustmentItemService {

    @Override
    public void saveInventoryAdjustmentItems(List<InventoryAdjustmentItemBo> itemsBo) {
        if (itemsBo == null || itemsBo.isEmpty()) {
            return;
        }
        List<InventoryAdjustmentItem> items = MapstructUtils.convert(itemsBo, InventoryAdjustmentItem.class);
        saveBatch(items);
    }

    @Override
    public void replaceInventoryAdjustmentItems(Long orderId, List<InventoryAdjustmentItemBo> itemsBo) {
        remove(Wrappers.<InventoryAdjustmentItem>lambdaQuery().eq(InventoryAdjustmentItem::getOrderId, orderId));
        saveInventoryAdjustmentItems(itemsBo == null ? Collections.emptyList() : itemsBo);
    }
}

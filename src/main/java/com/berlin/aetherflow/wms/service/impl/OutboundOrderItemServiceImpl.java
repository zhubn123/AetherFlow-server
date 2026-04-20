package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderItemBo;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import com.berlin.aetherflow.wms.mapper.OutboundOrderItemMapper;
import com.berlin.aetherflow.wms.service.OutboundOrderItemService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author berlin
* @description 针对表【outbound_order_item(出库单明细)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
public class OutboundOrderItemServiceImpl extends ServiceImpl<OutboundOrderItemMapper, OutboundOrderItem>
        implements OutboundOrderItemService {

    @Override
    public void saveOutboundOrderItems(List<OutboundOrderItemBo> itemsBo) {
        if (itemsBo == null || itemsBo.isEmpty()) {
            return;
        }
        List<OutboundOrderItem> items = MapstructUtils.convert(itemsBo, OutboundOrderItem.class);
        saveBatch(items);
    }

    @Override
    public void replaceOutboundOrderItems(Long orderId, List<OutboundOrderItemBo> itemsBo) {
        remove(Wrappers.<OutboundOrderItem>lambdaQuery().eq(OutboundOrderItem::getOrderId, orderId));
        saveOutboundOrderItems(itemsBo == null ? Collections.emptyList() : itemsBo);
    }

}





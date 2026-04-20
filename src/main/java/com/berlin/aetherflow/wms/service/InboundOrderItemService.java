package com.berlin.aetherflow.wms.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderItemBo;
import com.berlin.aetherflow.wms.domain.entity.InboundOrderItem;

import java.util.List;

/**
* @author berlin
* @description 针对表【inbound_order_item(入库单明细)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface InboundOrderItemService extends IService<InboundOrderItem> {

    void saveInboundOrderItems(List<InboundOrderItemBo> itemsBo);

    void replaceInboundOrderItems(Long orderId, List<InboundOrderItemBo> itemsBo);

}

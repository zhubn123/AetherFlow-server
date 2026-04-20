package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderBo;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.wms.domain.query.InboundOrderQuery;

/**
 * @author berlin
 * @description 针对表【inbound_order(入库单)】的数据库操作Service
 * @createDate 2026-04-15 16:17:27
 */
public interface InboundOrderService extends IService<InboundOrder> {

    Object queryList(InboundOrderQuery query);

    int createInboundOrder(InboundOrderBo bo);
}

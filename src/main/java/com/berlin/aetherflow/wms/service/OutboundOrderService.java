package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderBo;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.query.OutboundOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.OutboundOrderVo;

import java.util.List;

/**
* @author berlin
* @description 针对表【outbound_order(出库单)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface OutboundOrderService extends IService<OutboundOrder> {

    PageResult<OutboundOrderVo> queryList(OutboundOrderQuery query);

    Long createOutboundOrder(OutboundOrderBo bo);

    Boolean updateOutboundOrder(OutboundOrderBo bo);

    Boolean applyAction(Long id, OutboundOrderActionBo bo);

    Boolean removeOutboundOrders(List<Long> ids);
}

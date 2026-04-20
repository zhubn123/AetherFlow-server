package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderBo;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.wms.domain.query.InboundOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.InboundOrderVo;

/**
 * @author berlin
 * @description 针对表【inbound_order(入库单)】的数据库操作Service
 * @createDate 2026-04-15 16:17:27
 */
public interface InboundOrderService extends IService<InboundOrder> {

    PageResult<InboundOrderVo> queryList(InboundOrderQuery query);

    Long createInboundOrder(InboundOrderBo bo);

    Boolean updateInboundOrder(InboundOrderBo bo);

    Boolean applyAction(Long id, InboundOrderActionBo bo);
}

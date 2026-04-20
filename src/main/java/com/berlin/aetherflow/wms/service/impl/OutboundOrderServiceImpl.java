package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.service.OutboundOrderService;
import org.springframework.stereotype.Service;

/**
* @author berlin
* @description 针对表【outbound_order(出库单)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder>
    implements OutboundOrderService {

}





package com.berlin.aetherflow.modules.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.modules.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.modules.wms.mapper.InboundOrderMapper;
import com.berlin.aetherflow.modules.wms.service.InboundOrderService;
import org.springframework.stereotype.Service;

/**
* @author berlin
* @description 针对表【inbound_order(入库单)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder>
    implements InboundOrderService{

}





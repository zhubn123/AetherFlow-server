package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.wms.domain.entity.InboundOrderItem;
import com.berlin.aetherflow.wms.mapper.InboundOrderItemMapper;
import com.berlin.aetherflow.wms.service.InboundOrderItemService;
import org.springframework.stereotype.Service;

/**
* @author berlin
* @description 针对表【inbound_order_item(入库单明细)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
public class InboundOrderItemServiceImpl extends ServiceImpl<InboundOrderItemMapper, InboundOrderItem>
    implements InboundOrderItemService{

}





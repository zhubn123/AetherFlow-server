package com.berlin.aetherflow.modules.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.modules.wms.domain.entity.OutboundOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
* @author berlin
* @description 针对表【outbound_order_item(出库单明细)】的数据库操作Mapper
* @createDate 2026-04-15 16:17:27
* @Entity com.berlin.aetherflow.modules.wms.domain.entity.OutboundOrderItem
*/
@Mapper
public interface OutboundOrderItemMapper extends BaseMapper<OutboundOrderItem> {

}





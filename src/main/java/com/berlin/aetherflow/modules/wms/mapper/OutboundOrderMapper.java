package com.berlin.aetherflow.modules.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.modules.wms.domain.entity.OutboundOrder;
import org.apache.ibatis.annotations.Mapper;

/**
* @author berlin
* @description 针对表【outbound_order(出库单)】的数据库操作Mapper
* @createDate 2026-04-15 16:17:27
* @Entity com.berlin.aetherflow.modules.wms.domain.entity.OutboundOrder
*/
@Mapper
public interface OutboundOrderMapper extends BaseMapper<OutboundOrder> {

}





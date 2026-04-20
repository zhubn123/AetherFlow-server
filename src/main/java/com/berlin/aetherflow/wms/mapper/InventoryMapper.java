package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;

/**
* @author berlin
* @description 针对表【stock(库存表)】的数据库操作Mapper
* @createDate 2026-04-15 16:17:27
* @Entity com.berlin.aetherflow.wms.domain.entity.Inventory
*/
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

}





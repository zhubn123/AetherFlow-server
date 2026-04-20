package com.berlin.aetherflow.wms.mapper;

import com.berlin.aetherflow.common.BaseMapperPlus;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;

/**
* @author berlin
* @description 针对表【warehouse(仓库表)】的数据库操作Mapper
* @createDate 2026-04-15 16:17:27
* @Entity com.berlin.aetherflow.wms.domain.entity.Warehouse
*/
@Mapper
public interface WarehouseMapper extends BaseMapperPlus<Warehouse> {

}





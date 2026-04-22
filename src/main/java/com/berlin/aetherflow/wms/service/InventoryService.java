package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.wms.domain.vo.InventoryVo;

/**
* @author berlin
* @description 针对表【stock(库存表)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface InventoryService extends IService<Inventory> {

    PageResult<InventoryVo> queryList(InventoryQuery query);
}

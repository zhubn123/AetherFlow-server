package com.berlin.aetherflow.modules.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.modules.wms.domain.bo.InventoryBo;
import com.berlin.aetherflow.modules.wms.domain.entity.Inventory;
import com.berlin.aetherflow.modules.wms.domain.vo.InventoryVo;

import java.util.List;

/**
* @author berlin
* @description 针对表【stock(库存表)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface InventoryService extends IService<Inventory> {

    List<InventoryVo> queryList(IPage<Inventory> page, InventoryBo bo);
}

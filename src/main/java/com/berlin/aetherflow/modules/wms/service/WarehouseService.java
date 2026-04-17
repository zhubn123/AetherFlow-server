package com.berlin.aetherflow.modules.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.modules.wms.domain.query.WarehouseQuery;
import com.berlin.aetherflow.modules.wms.domain.bo.WarehouseBo;
import com.berlin.aetherflow.modules.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.modules.wms.domain.vo.WarehouseVo;

import java.util.List;

/**
* @author berlin
* @description 针对表【warehouse(仓库表)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface WarehouseService extends IService<Warehouse> {

    WarehouseVo getByCode(String code);

    List<WarehouseVo> queryList(WarehouseQuery query);

    void createWarehouse(WarehouseBo bo);

    void updateWarehouse(WarehouseBo bo);

    void deleteWarehouseByIds(List<Long> ids);
}

package com.berlin.aetherflow.wms.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.LocationBo;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.query.LocationQuery;
import com.berlin.aetherflow.wms.domain.vo.LocationVo;

import java.util.List;

/**
* @author berlin
* @description 针对表【location(库位表)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface LocationService extends IService<Location> {

    PageResult<LocationVo> queryList(LocationQuery query);

    Long createLocation(LocationBo bo);

    Boolean updateLocation(LocationBo bo);

    Boolean removeLocations(List<Long> ids);
}

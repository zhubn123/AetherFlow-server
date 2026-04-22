package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.AreaBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.query.AreaQuery;
import com.berlin.aetherflow.wms.domain.vo.AreaVo;

import java.util.List;

/**
 * 区域 Service。
 */
public interface AreaService extends IService<Area> {

    PageResult<AreaVo> queryList(AreaQuery query);

    Long createArea(AreaBo bo);

    Boolean updateArea(AreaBo bo);

    Boolean removeAreas(List<Long> ids);
}

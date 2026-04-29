package com.berlin.aetherflow.wms.service;

import com.berlin.aetherflow.wms.domain.query.WmsOptionQuery;
import com.berlin.aetherflow.wms.domain.vo.WmsOptionVo;

import java.util.List;

/**
 * WMS 主数据选项服务。
 */
public interface WmsOptionService {

    List<WmsOptionVo> queryWarehouseOptions(WmsOptionQuery query);

    List<WmsOptionVo> queryAreaOptions(WmsOptionQuery query);

    List<WmsOptionVo> queryLocationOptions(WmsOptionQuery query);

    List<WmsOptionVo> queryMaterialOptions(WmsOptionQuery query);
}

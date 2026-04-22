package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.MaterialBo;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.query.MaterialQuery;
import com.berlin.aetherflow.wms.domain.vo.MaterialVo;

import java.util.List;

/**
* @author berlin
* @description 针对表【material(物料表)】的数据库操作Service
* @createDate 2026-04-15 16:17:27
*/
public interface MaterialService extends IService<Material> {

    PageResult<MaterialVo> queryList(MaterialQuery query);

    Long createMaterial(MaterialBo bo);

    Boolean updateMaterial(MaterialBo bo);

    Boolean removeMaterials(List<Long> ids);
}

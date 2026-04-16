package com.berlin.aetherflow.modules.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.modules.wms.domain.entity.Material;
import com.berlin.aetherflow.modules.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.modules.wms.service.MaterialService;
import org.springframework.stereotype.Service;

/**
* @author berlin
* @description 针对表【material(物料表)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material>
    implements MaterialService{

}





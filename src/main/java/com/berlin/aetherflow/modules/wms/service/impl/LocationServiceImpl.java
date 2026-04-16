package com.berlin.aetherflow.modules.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.modules.wms.domain.entity.Location;
import com.berlin.aetherflow.modules.wms.mapper.LocationMapper;
import com.berlin.aetherflow.modules.wms.service.LocationService;
import org.springframework.stereotype.Service;

/**
* @author berlin
* @description 针对表【location(库位表)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location>
    implements LocationService{

}





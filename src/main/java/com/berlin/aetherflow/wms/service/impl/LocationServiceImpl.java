package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.constant.BizCodeTypeConst;
import com.berlin.aetherflow.wms.domain.bo.LocationBo;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.LocationQuery;
import com.berlin.aetherflow.wms.domain.vo.LocationVo;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.LocationService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author berlin
* @description 针对表【location(库位表)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
@AllArgsConstructor
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location>
        implements LocationService {

    private final LocationMapper locationMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public PageResult<LocationVo> queryList(LocationQuery query) {
        IPage<Location> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<Location> lqw = new LambdaQueryWrapper<>();
        if (query.getWarehouseId() != null) {
            lqw.eq(Location::getWarehouseId, query.getWarehouseId());
        }
        if (StringUtils.isNotBlank(query.getLocationCode())) {
            lqw.eq(Location::getLocationCode, query.getLocationCode());
        }
        if (StringUtils.isNotBlank(query.getLocationName())) {
            lqw.like(Location::getLocationName, query.getLocationName());
        }
        if (query.getStatus() != null) {
            lqw.eq(Location::getStatus, query.getStatus());
        }
        if (StringUtils.isNotBlank(query.getRemark())) {
            lqw.like(Location::getRemark, query.getRemark());
        }

        IPage<Location> result = locationMapper.selectPage(page, lqw);
        List<LocationVo> records = result.getRecords().stream()
                .map(e -> MapstructUtils.convert(e, LocationVo.class))
                .toList();
        fillWarehouseDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public Long createLocation(LocationBo bo) {
        Location location = MapstructUtils.convert(bo, Location.class);
        if (location.getStatus() == null) {
            location.setStatus(0);
        }
        location.setLocationCode(CodeGenerate.generateSimple(BizCodeTypeConst.LOCATION));
        locationMapper.insert(location);
        return location.getId();
    }

    @Override
    public Boolean updateLocation(LocationBo bo) {
        Location exists = getById(bo.getId());
        if (Objects.isNull(exists)) {
            throw new RuntimeException("库位不存在");
        }
        Location location = MapstructUtils.convert(bo, Location.class);
        location.setLocationCode(null);
        return updateById(location);
    }

    @Override
    public Boolean removeLocations(List<Long> ids) {
        return removeByIds(ids);
    }

    /**
     * 填充库位列表的仓库展示字段（编码、名称）。
     *
     * @param records 库位列表
     */
    private void fillWarehouseDisplay(List<LocationVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(LocationVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (warehouseIds.isEmpty()) {
            return;
        }

        Map<Long, Warehouse> warehouseMap = warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));

        for (LocationVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse == null) {
                continue;
            }
            record.setWarehouseCode(warehouse.getWarehouseCode());
            record.setWarehouseName(warehouse.getWarehouseName());
        }
    }

}





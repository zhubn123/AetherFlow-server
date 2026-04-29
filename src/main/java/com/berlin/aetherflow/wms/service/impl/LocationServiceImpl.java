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
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.LocationQuery;
import com.berlin.aetherflow.wms.domain.vo.LocationVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.LocationService;
import com.berlin.aetherflow.wms.support.WmsOptionCacheSupport;
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
    private final AreaMapper areaMapper;
    private final WmsOptionCacheSupport wmsOptionCacheSupport;

    @Override
    public PageResult<LocationVo> queryList(LocationQuery query) {
        IPage<Location> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<Location> lqw = new LambdaQueryWrapper<>();
        if (query.getWarehouseId() != null) {
            lqw.eq(Location::getWarehouseId, query.getWarehouseId());
        }
        if (query.getAreaId() != null) {
            lqw.eq(Location::getAreaId, query.getAreaId());
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
        fillDisplayFields(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public Long createLocation(LocationBo bo) {
        validateAreaBelongsWarehouse(bo.getWarehouseId(), bo.getAreaId());
        Location location = MapstructUtils.convert(bo, Location.class);
        if (location.getStatus() == null) {
            location.setStatus(0);
        }
        location.setLocationCode(CodeGenerate.generateSimple(BizCodeTypeConst.LOCATION));
        locationMapper.insert(location);
        wmsOptionCacheSupport.evictLocationOptions();
        return location.getId();
    }

    @Override
    public Boolean updateLocation(LocationBo bo) {
        Location exists = getById(bo.getId());
        if (Objects.isNull(exists)) {
            throw new RuntimeException("库位不存在");
        }
        validateAreaBelongsWarehouse(bo.getWarehouseId(), bo.getAreaId());
        Location location = MapstructUtils.convert(bo, Location.class);
        location.setLocationCode(null);
        boolean updated = updateById(location);
        if (updated) {
            wmsOptionCacheSupport.evictLocationOptions();
        }
        return updated;
    }

    @Override
    public Boolean removeLocations(List<Long> ids) {
        boolean removed = removeByIds(ids);
        if (removed && ids != null && !ids.isEmpty()) {
            wmsOptionCacheSupport.evictLocationOptions();
        }
        return removed;
    }

    /**
     * 填充库位列表的仓库展示字段（编码、名称）。
     *
     * @param records 库位列表
     */
    private void fillDisplayFields(List<LocationVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(LocationVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> areaIds = records.stream()
                .map(LocationVo::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Warehouse> warehouseMap = warehouseIds.isEmpty()
                ? Map.of()
                : warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        Map<Long, Area> areaMap = areaIds.isEmpty()
                ? Map.of()
                : areaMapper.selectByIds(areaIds).stream()
                .collect(Collectors.toMap(Area::getId, area -> area, (left, right) -> left));

        for (LocationVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }
            Area area = areaMap.get(record.getAreaId());
            if (area != null) {
                record.setAreaCode(area.getAreaCode());
                record.setAreaName(area.getAreaName());
            }
        }
    }

    private void validateAreaBelongsWarehouse(Long warehouseId, Long areaId) {
        if (warehouseId == null) {
            throw new RuntimeException("仓库不能为空");
        }
        if (areaId == null) {
            throw new RuntimeException("区域不能为空");
        }
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }
        Area area = areaMapper.selectById(areaId);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        if (!Objects.equals(area.getWarehouseId(), warehouseId)) {
            throw new RuntimeException("区域不属于当前仓库");
        }
    }

}





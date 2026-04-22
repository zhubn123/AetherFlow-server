package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.wms.domain.vo.InventoryVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author berlin
* @description 针对表【Inventory(库存表)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
@AllArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
    implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;
    private final AreaMapper areaMapper;

    @Override
    public PageResult<InventoryVo> queryList(InventoryQuery query) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(query.getLocationId() != null, Inventory::getLocationId, query.getLocationId())
                .eq(query.getWarehouseId() != null, Inventory::getWarehouseId, query.getWarehouseId())
                .eq(query.getMaterialId() != null, Inventory::getMaterialId, query.getMaterialId())
                .ge(query.getMinQuantity() != null, Inventory::getQuantity, query.getMinQuantity())
                .le(query.getMaxQuantity() != null, Inventory::getQuantity, query.getMaxQuantity());

        if (query.getAreaId() != null) {
            LambdaQueryWrapper<Location> locationLqw = Wrappers.<Location>lambdaQuery()
                    .select(Location::getId)
                    .eq(Location::getAreaId, query.getAreaId())
                    .eq(query.getWarehouseId() != null, Location::getWarehouseId, query.getWarehouseId());
            List<Long> matchedLocationIds = locationMapper.selectList(locationLqw).stream()
                    .map(Location::getId)
                    .toList();
            if (matchedLocationIds.isEmpty()) {
                return PageResult.of(Long.valueOf(query.getPageNo()), Long.valueOf(query.getPageSize()), 0L, 0L, List.of());
            }
            lqw.in(Inventory::getLocationId, matchedLocationIds);
        }

        IPage<Inventory> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<Inventory> result = inventoryMapper.selectPage(page, lqw);
        List<InventoryVo> records = result.getRecords().stream()
                .map(e -> MapstructUtils.convert(e, InventoryVo.class))
                .toList();
        fillDisplayFields(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    /**
     * 填充库存列表的展示字段（仓库/库位/物料编码和名称）。
     *
     * @param records 库存列表
     */
    private void fillDisplayFields(List<InventoryVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(InventoryVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = records.stream()
                .map(InventoryVo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> materialIds = records.stream()
                .map(InventoryVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Warehouse> warehouseMap = warehouseIds.isEmpty()
                ? Map.of()
                : warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        Map<Long, Location> locationMap = locationIds.isEmpty()
                ? Map.of()
                : locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
        Set<Long> areaIds = locationMap.values().stream()
                .map(Location::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Area> areaMap = areaIds.isEmpty()
                ? Map.of()
                : areaMapper.selectByIds(areaIds).stream()
                .collect(Collectors.toMap(Area::getId, area -> area, (left, right) -> left));
        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));

        for (InventoryVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }

            Location location = locationMap.get(record.getLocationId());
            if (location != null) {
                record.setAreaId(location.getAreaId());
                record.setLocationCode(location.getLocationCode());
                record.setLocationName(location.getLocationName());
                Area area = areaMap.get(location.getAreaId());
                if (area != null) {
                    record.setAreaCode(area.getAreaCode());
                    record.setAreaName(area.getAreaName());
                }
            }

            Material material = materialMap.get(record.getMaterialId());
            if (material != null) {
                record.setMaterialCode(material.getMaterialCode());
                record.setMaterialName(material.getMaterialName());
            }
        }
    }
}





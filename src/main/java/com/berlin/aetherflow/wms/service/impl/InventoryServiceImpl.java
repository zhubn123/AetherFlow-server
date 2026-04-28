package com.berlin.aetherflow.wms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
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
import com.berlin.aetherflow.wms.service.StockTransactionService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final StockTransactionService stockTransactionService;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyStockChanges(List<StockChangeBo> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        for (StockChangeBo change : changes) {
            if (change == null) {
                continue;
            }
            validateStockChange(change);

            Location location = locationMapper.selectById(change.getLocationId());
            if (location == null) {
                throw new RuntimeException("库存变动库位不存在: " + change.getLocationId());
            }
            if (!Objects.equals(location.getWarehouseId(), change.getWarehouseId())) {
                throw new RuntimeException("库存变动仓库与库位不一致: " + change.getLocationId());
            }

            if (change.getChangeQty().compareTo(BigDecimal.ZERO) > 0) {
                applyInboundChange(change, location);
                continue;
            }
            applyOutboundChange(change, location);
        }
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

    private void validateStockChange(StockChangeBo change) {
        if (StringUtils.isBlank(change.getBizType())) {
            throw new RuntimeException("库存变动业务类型不能为空");
        }
        if (change.getBizId() == null) {
            throw new RuntimeException("库存变动业务单据ID不能为空");
        }
        if (change.getWarehouseId() == null) {
            throw new RuntimeException("库存变动仓库不能为空");
        }
        if (change.getLocationId() == null) {
            throw new RuntimeException("库存变动库位不能为空");
        }
        if (change.getMaterialId() == null) {
            throw new RuntimeException("库存变动物料不能为空");
        }
        if (change.getChangeQty() == null || change.getChangeQty().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("库存变动数量不能为0");
        }
    }

    private void applyInboundChange(StockChangeBo change, Location location) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, change.getWarehouseId())
                .eq(Inventory::getLocationId, change.getLocationId())
                .eq(Inventory::getMaterialId, change.getMaterialId());
        Inventory inventory = inventoryMapper.selectOne(lqw);
        BigDecimal beforeQty = inventory == null || inventory.getQuantity() == null
                ? BigDecimal.ZERO
                : inventory.getQuantity();
        BigDecimal afterQty = beforeQty.add(change.getChangeQty());

        if (inventory == null) {
            Inventory toCreate = new Inventory();
            toCreate.setWarehouseId(change.getWarehouseId());
            toCreate.setLocationId(change.getLocationId());
            toCreate.setMaterialId(change.getMaterialId());
            toCreate.setQuantity(afterQty);
            toCreate.setLockedQuantity(BigDecimal.ZERO);
            boolean saved = save(toCreate);
            if (!saved) {
                throw new RuntimeException("库存创建失败");
            }
        } else {
            inventory.setQuantity(afterQty);
            if (inventory.getLockedQuantity() == null) {
                inventory.setLockedQuantity(BigDecimal.ZERO);
            }
            boolean updated = updateById(inventory);
            if (!updated) {
                throw new RuntimeException("库存更新失败");
            }
        }

        stockTransactionService.createTransaction(change, location.getAreaId(), beforeQty, afterQty);
    }

    private void applyOutboundChange(StockChangeBo change, Location location) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, change.getWarehouseId())
                .eq(Inventory::getLocationId, change.getLocationId())
                .eq(Inventory::getMaterialId, change.getMaterialId());
        Inventory inventory = inventoryMapper.selectOne(lqw);
        if (inventory == null) {
            throw new RuntimeException(resolveMissingOutboundInventoryMessage(change));
        }

        BigDecimal deductQty = change.getChangeQty().abs();
        int updated = inventoryMapper.deductAvailableQuantity(inventory.getId(), deductQty, resolveOperator());
        if (updated != 1) {
            throw new RuntimeException(resolveOutboundDeductionFailureMessage(change, deductQty, inventory.getId()));
        }

        Inventory latestInventory = inventoryMapper.selectById(inventory.getId());
        if (latestInventory == null || latestInventory.getQuantity() == null) {
            throw new RuntimeException(withLinePrefix(change, "库存扣减成功后未查询到最新库存，请重试"));
        }
        BigDecimal afterQty = latestInventory.getQuantity();
        BigDecimal beforeQty = afterQty.add(deductQty);
        stockTransactionService.createTransaction(change, location.getAreaId(), beforeQty, afterQty);
    }

    private String resolveMissingOutboundInventoryMessage(StockChangeBo change) {
        Long locationInventoryCount = inventoryMapper.selectCount(Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, change.getWarehouseId())
                .eq(Inventory::getLocationId, change.getLocationId())
                .gt(Inventory::getQuantity, BigDecimal.ZERO));
        if (locationInventoryCount != null && locationInventoryCount > 0) {
            return withLinePrefix(change, "库位不存在当前物料库存，物料不匹配");
        }
        return withLinePrefix(change, "库位无库存，无法扣减");
    }

    private String resolveOutboundDeductionFailureMessage(StockChangeBo change, BigDecimal deductQty, Long inventoryId) {
        Inventory latestInventory = inventoryMapper.selectById(inventoryId);
        if (latestInventory == null) {
            return withLinePrefix(change, "库存记录不存在，无法扣减");
        }
        BigDecimal quantity = latestInventory.getQuantity() == null ? BigDecimal.ZERO : latestInventory.getQuantity();
        BigDecimal lockedQuantity = latestInventory.getLockedQuantity() == null ? BigDecimal.ZERO : latestInventory.getLockedQuantity();
        BigDecimal availableQuantity = quantity.subtract(lockedQuantity);
        if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return withLinePrefix(change, "库位无可用库存，无法扣减");
        }
        if (availableQuantity.compareTo(deductQty) < 0) {
            return withLinePrefix(change, "库存不足，无法扣减");
        }
        return withLinePrefix(change, "库存扣减失败，请重试");
    }

    private String withLinePrefix(StockChangeBo change, String message) {
        if (change.getLineNo() == null) {
            return message;
        }
        return "行号 " + change.getLineNo() + "：" + message;
    }

    private String resolveOperator() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                Object operatorName = StpUtil.getTokenSession().get("operatorName");
                if (operatorName != null && StringUtils.isNotBlank(String.valueOf(operatorName))) {
                    return String.valueOf(operatorName);
                }
                return String.valueOf(loginId);
            }
        } catch (Exception ex) {
            return "system";
        }
        return "system";
    }
}





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
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.StockTransaction;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.StockTransactionQuery;
import com.berlin.aetherflow.wms.domain.vo.StockTransactionVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.StockTransactionMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.StockTransactionService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 库存流水 Service 实现。
 */
@Service
@AllArgsConstructor
public class StockTransactionServiceImpl extends ServiceImpl<StockTransactionMapper, StockTransaction>
        implements StockTransactionService {

    private final StockTransactionMapper stockTransactionMapper;
    private final WarehouseMapper warehouseMapper;
    private final AreaMapper areaMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;

    @Override
    public PageResult<StockTransactionVo> queryList(StockTransactionQuery query) {
        LambdaQueryWrapper<StockTransaction> lqw = Wrappers.<StockTransaction>lambdaQuery()
                .eq(query.getId() != null, StockTransaction::getId, query.getId())
                .eq(StringUtils.isNotBlank(query.getBizType()), StockTransaction::getBizType, query.getBizType())
                .eq(query.getBizId() != null, StockTransaction::getBizId, query.getBizId())
                .eq(query.getWarehouseId() != null, StockTransaction::getWarehouseId, query.getWarehouseId())
                .eq(query.getAreaId() != null, StockTransaction::getAreaId, query.getAreaId())
                .eq(query.getLocationId() != null, StockTransaction::getLocationId, query.getLocationId())
                .eq(query.getMaterialId() != null, StockTransaction::getMaterialId, query.getMaterialId())
                .eq(query.getOperatorId() != null, StockTransaction::getOperatorId, query.getOperatorId())
                .ge(query.getOperateStartTime() != null, StockTransaction::getOperateTime, query.getOperateStartTime())
                .le(query.getOperateEndTime() != null, StockTransaction::getOperateTime, query.getOperateEndTime())
                .like(StringUtils.isNotBlank(query.getRemark()), StockTransaction::getRemark, query.getRemark());
        if (StringUtils.isBlank(query.getSortBy()) || query.getIsAsc() == null) {
            lqw.orderByDesc(StockTransaction::getOperateTime, StockTransaction::getId);
        }

        IPage<StockTransaction> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<StockTransaction> result = stockTransactionMapper.selectPage(page, lqw);
        List<StockTransactionVo> records = result.getRecords().stream()
                .map(this::toDetailVo)
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public StockTransactionVo getDetailById(Long id) {
        StockTransaction transaction = getById(id);
        if (transaction == null) {
            return null;
        }
        return toDetailVo(transaction);
    }

    @Override
    public void createTransaction(StockChangeBo change, Long areaId, BigDecimal beforeQty, BigDecimal afterQty) {
        StockTransaction transaction = new StockTransaction();
        transaction.setBizType(change.getBizType());
        transaction.setBizId(change.getBizId());
        transaction.setWarehouseId(change.getWarehouseId());
        transaction.setAreaId(areaId);
        transaction.setLocationId(change.getLocationId());
        transaction.setMaterialId(change.getMaterialId());
        transaction.setChangeQty(change.getChangeQty());
        transaction.setBeforeQty(beforeQty);
        transaction.setAfterQty(afterQty);
        transaction.setOperatorId(resolveOperatorId());
        transaction.setOperateTime(change.getOperateTime() != null ? change.getOperateTime() : LocalDateTime.now());
        transaction.setRemark(StringUtils.defaultString(change.getRemark()));
        boolean saved = save(transaction);
        if (!saved) {
            throw new RuntimeException("库存流水写入失败");
        }
    }

    private StockTransactionVo toDetailVo(StockTransaction transaction) {
        StockTransactionVo vo = MapstructUtils.convert(transaction, StockTransactionVo.class);
        if (vo == null) {
            return null;
        }
        fillDisplayFields(List.of(vo));
        return vo;
    }

    private void fillDisplayFields(List<StockTransactionVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(StockTransactionVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> areaIds = records.stream()
                .map(StockTransactionVo::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = records.stream()
                .map(StockTransactionVo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> materialIds = records.stream()
                .map(StockTransactionVo::getMaterialId)
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
        areaIds.addAll(locationMap.values().stream()
                .map(Location::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));
        Map<Long, Area> areaMap = areaIds.isEmpty()
                ? Map.of()
                : areaMapper.selectByIds(areaIds).stream()
                .collect(Collectors.toMap(Area::getId, area -> area, (left, right) -> left));

        for (StockTransactionVo record : records) {
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

            Location location = locationMap.get(record.getLocationId());
            if (location != null) {
                record.setLocationCode(location.getLocationCode());
                record.setLocationName(location.getLocationName());
                if (record.getAreaId() == null) {
                    record.setAreaId(location.getAreaId());
                    Area locationArea = areaMap.get(location.getAreaId());
                    if (locationArea != null) {
                        record.setAreaCode(locationArea.getAreaCode());
                        record.setAreaName(locationArea.getAreaName());
                    }
                }
            }

            Material material = materialMap.get(record.getMaterialId());
            if (material != null) {
                record.setMaterialCode(material.getMaterialCode());
                record.setMaterialName(material.getMaterialName());
            }

            record.setOperatorName(record.getCreateBy());
        }
    }

    private Long resolveOperatorId() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId == null) {
                return null;
            }
            return Long.parseLong(String.valueOf(loginId));
        } catch (Exception ex) {
            return null;
        }
    }
}

package com.berlin.aetherflow.wms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.constant.BizCodeTypeConst;
import com.berlin.aetherflow.wms.constant.InventoryAdjustmentTypeConst;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentActionBo;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentBo;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentItemBo;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustmentItem;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.InventoryAdjustmentQuery;
import com.berlin.aetherflow.wms.domain.vo.InventoryAdjustmentDetailVo;
import com.berlin.aetherflow.wms.domain.vo.InventoryAdjustmentItemVo;
import com.berlin.aetherflow.wms.domain.vo.InventoryAdjustmentVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.InventoryAdjustmentMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InventoryAdjustmentItemService;
import com.berlin.aetherflow.wms.service.InventoryAdjustmentService;
import com.berlin.aetherflow.wms.service.InventoryService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 库存调整单 Service 实现。
 */
@Service
@AllArgsConstructor
public class InventoryAdjustmentServiceImpl extends ServiceImpl<InventoryAdjustmentMapper, InventoryAdjustment>
        implements InventoryAdjustmentService {

    private final InventoryAdjustmentMapper inventoryAdjustmentMapper;
    private final InventoryAdjustmentItemService inventoryAdjustmentItemService;
    private final InventoryService inventoryService;
    private final WarehouseMapper warehouseMapper;
    private final AreaMapper areaMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;

    @Override
    public PageResult<InventoryAdjustmentVo> queryList(InventoryAdjustmentQuery query) {
        IPage<InventoryAdjustment> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<InventoryAdjustment> lqw = Wrappers.<InventoryAdjustment>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getOrderNo()), InventoryAdjustment::getOrderNo, query.getOrderNo())
                .eq(query.getWarehouseId() != null, InventoryAdjustment::getWarehouseId, query.getWarehouseId())
                .eq(query.getAreaId() != null, InventoryAdjustment::getAreaId, query.getAreaId())
                .eq(StringUtils.isNotBlank(query.getAdjustType()), InventoryAdjustment::getAdjustType, query.getAdjustType())
                .eq(query.getStatus() != null, InventoryAdjustment::getStatus, query.getStatus())
                .ge(query.getAdjustStartTime() != null, InventoryAdjustment::getAdjustTime, query.getAdjustStartTime())
                .le(query.getAdjustEndTime() != null, InventoryAdjustment::getAdjustTime, query.getAdjustEndTime())
                .like(StringUtils.isNotBlank(query.getAdjustReason()), InventoryAdjustment::getAdjustReason, query.getAdjustReason())
                .like(StringUtils.isNotBlank(query.getRemark()), InventoryAdjustment::getRemark, query.getRemark());

        IPage<InventoryAdjustment> result = inventoryAdjustmentMapper.selectPage(page, lqw);
        List<InventoryAdjustmentVo> records = result.getRecords().stream()
                .map(item -> MapstructUtils.convert(item, InventoryAdjustmentVo.class))
                .toList();
        fillHeaderDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public InventoryAdjustmentDetailVo getDetailById(Long id) {
        InventoryAdjustment order = getById(id);
        if (order == null) {
            return null;
        }

        InventoryAdjustmentDetailVo detailVo = MapstructUtils.convert(order, InventoryAdjustmentDetailVo.class);
        if (detailVo == null) {
            return null;
        }

        fillHeaderDisplay(List.of(detailVo));
        List<InventoryAdjustmentItem> items = inventoryAdjustmentItemService.lambdaQuery()
                .eq(InventoryAdjustmentItem::getOrderId, id)
                .orderByAsc(InventoryAdjustmentItem::getLineNo)
                .list();
        List<InventoryAdjustmentItemVo> itemVos = items.stream()
                .map(item -> MapstructUtils.convert(item, InventoryAdjustmentItemVo.class))
                .toList();
        fillItemDisplay(itemVos);
        detailVo.setAdjustmentItems(itemVos);
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInventoryAdjustment(InventoryAdjustmentBo bo) {
        validateAdjustType(bo.getAdjustType());
        validateAreaOwnership(bo.getWarehouseId(), bo.getAreaId());

        bo.setStatus(OrderStatusConst.DRAFT);
        bo.setAdjustTime(null);
        bo.setOrderNo(CodeGenerate.generateSimple(BizCodeTypeConst.INVENTORY_ADJUSTMENT));
        InventoryAdjustment order = MapstructUtils.convert(bo, InventoryAdjustment.class);
        inventoryAdjustmentMapper.insert(order);

        List<InventoryAdjustmentItemBo> itemsBo = normalizeAdjustmentItems(order.getId(), bo.getAdjustmentItemsBo());
        validateAdjustmentItems(order.getWarehouseId(), order.getAreaId(), itemsBo);
        inventoryAdjustmentItemService.saveInventoryAdjustmentItems(itemsBo);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateInventoryAdjustment(InventoryAdjustmentBo bo) {
        validateAdjustType(bo.getAdjustType());

        InventoryAdjustment order = getById(bo.getId());
        if (order == null) {
            throw new RuntimeException("库存调整单不存在");
        }
        if (OrderStatusConst.CONFIRMED.equals(order.getStatus())) {
            throw new RuntimeException("已确认单据不允许编辑");
        }

        Long warehouseIdForValidation = bo.getWarehouseId() != null ? bo.getWarehouseId() : order.getWarehouseId();
        Long areaIdForValidation = bo.getAreaId() != null ? bo.getAreaId() : order.getAreaId();
        validateAreaOwnership(warehouseIdForValidation, areaIdForValidation);

        InventoryAdjustment toUpdate = MapstructUtils.convert(bo, InventoryAdjustment.class);
        toUpdate.setStatus(null);
        toUpdate.setAdjustTime(null);
        boolean updated = updateById(toUpdate);
        if (!updated) {
            throw new RuntimeException("库存调整单更新失败");
        }

        if (bo.getAdjustmentItemsBo() != null) {
            List<InventoryAdjustmentItemBo> itemsBo = normalizeAdjustmentItems(bo.getId(), bo.getAdjustmentItemsBo());
            validateAdjustmentItems(warehouseIdForValidation, areaIdForValidation, itemsBo);
            inventoryAdjustmentItemService.replaceInventoryAdjustmentItems(bo.getId(), itemsBo);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, InventoryAdjustmentActionBo bo) {
        String action = bo.getAction();
        if (StringUtils.isBlank(action)) {
            throw new RuntimeException("动作不能为空");
        }
        action = action.trim().toUpperCase(Locale.ROOT);

        if ("CONFIRM".equals(action)) {
            return confirmInventoryAdjustment(id);
        }

        throw new RuntimeException("不支持的动作: " + action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeInventoryAdjustments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        List<InventoryAdjustment> orders = lambdaQuery()
                .in(InventoryAdjustment::getId, ids)
                .list();
        if (orders.isEmpty()) {
            return true;
        }

        InventoryAdjustment confirmedOrder = orders.stream()
                .filter(order -> OrderStatusConst.CONFIRMED.equals(order.getStatus()))
                .findFirst()
                .orElse(null);
        if (confirmedOrder != null) {
            throw new RuntimeException("已确认库存调整单不允许删除: " + confirmedOrder.getOrderNo());
        }

        List<Long> orderIds = orders.stream()
                .map(InventoryAdjustment::getId)
                .toList();
        inventoryAdjustmentItemService.remove(Wrappers.<InventoryAdjustmentItem>lambdaQuery()
                .in(InventoryAdjustmentItem::getOrderId, orderIds));
        return removeByIds(orderIds);
    }

    private Boolean confirmInventoryAdjustment(Long id) {
        InventoryAdjustment order = getById(id);
        if (order == null) {
            throw new RuntimeException("库存调整单不存在");
        }
        validateAdjustType(order.getAdjustType());
        if (!OrderStatusConst.DRAFT.equals(order.getStatus())) {
            if (OrderStatusConst.CONFIRMED.equals(order.getStatus())) {
                throw new RuntimeException("库存调整单已确认，请勿重复提交");
            }
            throw new RuntimeException("当前状态不可确认");
        }

        LocalDateTime confirmTime = LocalDateTime.now();
        int updatedRows = inventoryAdjustmentMapper.confirmDraftOrder(
                id,
                OrderStatusConst.DRAFT,
                OrderStatusConst.CONFIRMED,
                confirmTime,
                resolveOperator()
        );
        if (updatedRows != 1) {
            InventoryAdjustment latestOrder = getById(id);
            if (latestOrder != null && OrderStatusConst.CONFIRMED.equals(latestOrder.getStatus())) {
                throw new RuntimeException("库存调整单已确认，请勿重复提交");
            }
            throw new RuntimeException("当前状态不可确认");
        }

        List<InventoryAdjustmentItem> items = inventoryAdjustmentItemService.lambdaQuery()
                .eq(InventoryAdjustmentItem::getOrderId, id)
                .orderByAsc(InventoryAdjustmentItem::getLineNo)
                .list();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("库存调整单明细不能为空");
        }

        List<StockChangeBo> stockChanges = buildStockChanges(order, items, confirmTime);
        inventoryService.applyStockChanges(stockChanges);
        return true;
    }

    private List<InventoryAdjustmentItemBo> normalizeAdjustmentItems(Long orderId, List<InventoryAdjustmentItemBo> itemsBo) {
        if (itemsBo == null || itemsBo.isEmpty()) {
            throw new RuntimeException("库存调整单明细不能为空");
        }

        List<InventoryAdjustmentItemBo> normalizedItems = new ArrayList<>(itemsBo.size());
        for (int i = 0; i < itemsBo.size(); i++) {
            InventoryAdjustmentItemBo item = itemsBo.get(i);
            if (item == null) {
                continue;
            }
            item.setOrderId(orderId);
            if (item.getLineNo() == null) {
                item.setLineNo(i + 1);
            }
            if (Objects.isNull(item.getMaterialId())) {
                throw new RuntimeException("库存调整单明细物料不能为空");
            }
            if (Objects.isNull(item.getLocationId())) {
                throw new RuntimeException("库存调整单明细库位不能为空");
            }
            if (Objects.isNull(item.getAdjustQty())) {
                throw new RuntimeException("库存调整单明细调整数量不能为空");
            }
            if (item.getAdjustQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("库存调整单明细调整数量必须大于0，行号: " + item.getLineNo());
            }
            normalizedItems.add(item);
        }
        if (normalizedItems.isEmpty()) {
            throw new RuntimeException("库存调整单明细不能为空");
        }
        return normalizedItems;
    }

    private void validateAreaOwnership(Long warehouseId, Long areaId) {
        if (warehouseId == null) {
            throw new RuntimeException("仓库不能为空");
        }
        if (areaId == null) {
            throw new RuntimeException("区域不能为空");
        }
        Area area = areaMapper.selectById(areaId);
        if (area == null) {
            throw new RuntimeException("库存调整单区域不存在: " + areaId);
        }
        if (!Objects.equals(area.getWarehouseId(), warehouseId)) {
            throw new RuntimeException("库存调整单区域不属于当前仓库: " + area.getAreaCode());
        }
    }

    private void validateAdjustmentItems(Long warehouseId, Long areaId, List<InventoryAdjustmentItemBo> items) {
        if (warehouseId == null || areaId == null || items == null || items.isEmpty()) {
            return;
        }
        Set<Long> materialIds = items.stream()
                .map(InventoryAdjustmentItemBo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .map(InventoryAdjustmentItemBo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (materialIds.isEmpty() || locationIds.isEmpty()) {
            return;
        }
        Map<Long, Material> materialMap = materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));
        Map<Long, Location> locationMap = locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
        for (Long materialId : materialIds) {
            Material material = materialMap.get(materialId);
            if (material == null) {
                throw new RuntimeException("库存调整单明细存在无效物料: " + materialId);
            }
        }
        for (Long locationId : locationIds) {
            Location location = locationMap.get(locationId);
            if (location == null) {
                throw new RuntimeException("库存调整单明细存在无效库位: " + locationId);
            }
            if (!Objects.equals(location.getWarehouseId(), warehouseId)) {
                throw new RuntimeException("库存调整单明细库位不属于当前仓库: " + location.getLocationCode());
            }
            if (!Objects.equals(location.getAreaId(), areaId)) {
                throw new RuntimeException("库存调整单明细库位不属于当前区域: " + location.getLocationCode());
            }
        }
    }

    private List<StockChangeBo> buildStockChanges(InventoryAdjustment order, List<InventoryAdjustmentItem> items,
                                                  LocalDateTime operateTime) {
        List<StockChangeBo> changes = new ArrayList<>(items.size());
        for (InventoryAdjustmentItem item : items) {
            StockChangeBo change = new StockChangeBo();
            change.setBizType(StockBizTypeConst.INVENTORY_ADJUSTMENT);
            change.setBizId(order.getId());
            change.setWarehouseId(order.getWarehouseId());
            change.setLocationId(item.getLocationId());
            change.setMaterialId(item.getMaterialId());
            change.setLineNo(item.getLineNo());
            change.setChangeQty(resolveChangeQty(order.getAdjustType(), item.getAdjustQty(), item.getLineNo()));
            change.setOperateTime(operateTime);
            change.setRemark(buildTransactionRemark(order, item));
            changes.add(change);
        }
        return changes;
    }

    private BigDecimal resolveChangeQty(String adjustType, BigDecimal adjustQty, Integer lineNo) {
        if (adjustQty == null || adjustQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("库存调整单明细调整数量必须大于0，行号: " + lineNo);
        }
        if (InventoryAdjustmentTypeConst.INCREASE.equals(adjustType)) {
            return adjustQty;
        }
        if (InventoryAdjustmentTypeConst.DECREASE.equals(adjustType)) {
            return adjustQty.negate();
        }
        throw new RuntimeException("库存调整方向非法: " + adjustType);
    }

    private String buildTransactionRemark(InventoryAdjustment order, InventoryAdjustmentItem item) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(order.getAdjustReason())) {
            parts.add("原因: " + order.getAdjustReason().trim());
        }
        if (StringUtils.isNotBlank(item.getRemark())) {
            parts.add("明细备注: " + item.getRemark().trim());
        } else if (StringUtils.isNotBlank(order.getRemark())) {
            parts.add("单据备注: " + order.getRemark().trim());
        }
        return String.join("；", parts);
    }

    private void fillHeaderDisplay(List<? extends InventoryAdjustmentVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(InventoryAdjustmentVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> areaIds = records.stream()
                .map(InventoryAdjustmentVo::getAreaId)
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

        for (InventoryAdjustmentVo record : records) {
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

    private void fillItemDisplay(List<InventoryAdjustmentItemVo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Long> materialIds = items.stream()
                .map(InventoryAdjustmentItemVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .map(InventoryAdjustmentItemVo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));
        Map<Long, Location> locationMap = locationIds.isEmpty()
                ? Map.of()
                : locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));

        for (InventoryAdjustmentItemVo item : items) {
            Material material = materialMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialCode(material.getMaterialCode());
                item.setMaterialName(material.getMaterialName());
            }

            Location location = locationMap.get(item.getLocationId());
            if (location != null) {
                item.setLocationCode(location.getLocationCode());
                item.setLocationName(location.getLocationName());
            }
        }
    }

    private void validateAdjustType(String adjustType) {
        if (StringUtils.isBlank(adjustType)) {
            throw new RuntimeException("调整方向不能为空");
        }
        if (!InventoryAdjustmentTypeConst.INCREASE.equals(adjustType)
                && !InventoryAdjustmentTypeConst.DECREASE.equals(adjustType)) {
            throw new RuntimeException("不支持的调整方向: " + adjustType);
        }
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

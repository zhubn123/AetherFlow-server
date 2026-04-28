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
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderItemBo;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.OutboundOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.OutboundOrderVo;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InventoryService;
import com.berlin.aetherflow.wms.service.OutboundOrderItemService;
import com.berlin.aetherflow.wms.service.OutboundOrderService;
import com.berlin.aetherflow.wms.support.OutboundOrderConfirmLockSupport;
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
* @author berlin
* @description 针对表【outbound_order(出库单)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
@AllArgsConstructor
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder>
        implements OutboundOrderService {

    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundOrderItemService outboundOrderItemService;
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final InventoryService inventoryService;
    private final OutboundOrderConfirmLockSupport outboundOrderConfirmLockSupport;

    /**
     * 分页查询出库单
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<OutboundOrderVo> queryList(OutboundOrderQuery query) {
        IPage<OutboundOrder> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<OutboundOrder> lqw = Wrappers.<OutboundOrder>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getOrderNo()), OutboundOrder::getOrderNo, query.getOrderNo())
                .eq(query.getWarehouseId() != null, OutboundOrder::getWarehouseId, query.getWarehouseId())
                .eq(query.getStatus() != null, OutboundOrder::getStatus, query.getStatus())
                .ge(query.getOutboundStartTime() != null, OutboundOrder::getOutboundTime, query.getOutboundStartTime())
                .le(query.getOutboundEndTime() != null, OutboundOrder::getOutboundTime, query.getOutboundEndTime())
                .like(StringUtils.isNotBlank(query.getRemark()), OutboundOrder::getRemark, query.getRemark());
        if (query.getAreaId() != null) {
            lqw.inSql(OutboundOrder::getId,
                    "select distinct i.order_id from outbound_order_item i " +
                            "join location l on i.location_id = l.id " +
                            "where l.area_id = " + query.getAreaId());
        }

        IPage<OutboundOrder> result = outboundOrderMapper.selectPage(page, lqw);
        List<OutboundOrderVo> records = result.getRecords().stream()
                .map(e -> MapstructUtils.convert(e, OutboundOrderVo.class))
                .toList();
        fillWarehouseDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    /**
     * 暂存出库单
     *
     * @param bo 出库单参数
     * @return 出库单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOutboundOrder(OutboundOrderBo bo) {
        bo.setOrderNo(CodeGenerate.generateSimple(BizCodeTypeConst.OUTBOUND_ORDER));
        OutboundOrder order = MapstructUtils.convert(bo, OutboundOrder.class);
        outboundOrderMapper.insert(order);

        List<OutboundOrderItemBo> itemsBo = normalizeOrderItems(order.getId(), bo.getOrderItemsBo());
        validateOrderItemLocations(order.getWarehouseId(), itemsBo);
        outboundOrderItemService.saveOutboundOrderItems(itemsBo);
        return order.getId();
    }

    /**
     * 编辑出库单（仅草稿可编辑）
     *
     * @param bo 出库单参数
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOutboundOrder(OutboundOrderBo bo) {
        OutboundOrder order = getById(bo.getId());
        if (order == null) {
            throw new RuntimeException("出库单不存在");
        }
        if (OrderStatusConst.CONFIRMED.equals(order.getStatus())) {
            throw new RuntimeException("已确认单据不允许编辑");
        }

        OutboundOrder toUpdate = MapstructUtils.convert(bo, OutboundOrder.class);
        boolean updated = updateById(toUpdate);
        if (!updated) {
            throw new RuntimeException("出库单更新失败");
        }

        if (bo.getOrderItemsBo() != null) {
            List<OutboundOrderItemBo> normalizedItems = normalizeOrderItems(bo.getId(), bo.getOrderItemsBo());
            Long warehouseIdForValidation = toUpdate.getWarehouseId() != null ? toUpdate.getWarehouseId() : order.getWarehouseId();
            validateOrderItemLocations(warehouseIdForValidation, normalizedItems);
            outboundOrderItemService.replaceOutboundOrderItems(bo.getId(), normalizedItems);
        }
        return true;
    }

    /**
     * 状态流转（当前仅实现确认动作）
     *
     * @param id 单据ID
     * @param bo 动作参数
     * @return 执行结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, OutboundOrderActionBo bo) {
        String action = bo.getAction();
        if (StringUtils.isBlank(action)) {
            throw new RuntimeException("动作不能为空");
        }
        action = action.trim().toUpperCase(Locale.ROOT);

        if ("CONFIRM".equals(action)) {
            try (OutboundOrderConfirmLockSupport.LockHandle ignored = outboundOrderConfirmLockSupport.acquire(id)) {
                return confirmOutboundOrder(id);
            }
        }

        throw new RuntimeException("不支持的动作: " + action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeOutboundOrders(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        List<OutboundOrder> orders = lambdaQuery()
                .in(OutboundOrder::getId, ids)
                .list();
        if (orders.isEmpty()) {
            return true;
        }

        OutboundOrder confirmedOrder = orders.stream()
                .filter(order -> OrderStatusConst.CONFIRMED.equals(order.getStatus()))
                .findFirst()
                .orElse(null);
        if (confirmedOrder != null) {
            throw new RuntimeException("已确认出库单不允许删除: " + confirmedOrder.getOrderNo());
        }

        List<Long> orderIds = orders.stream()
                .map(OutboundOrder::getId)
                .toList();
        outboundOrderItemService.remove(Wrappers.<OutboundOrderItem>lambdaQuery()
                .in(OutboundOrderItem::getOrderId, orderIds));
        return removeByIds(orderIds);
    }

    /**
     * 标准化明细：补全 orderId、lineNo、shippedQty。
     */
    private List<OutboundOrderItemBo> normalizeOrderItems(Long orderId, List<OutboundOrderItemBo> itemsBo) {
        if (itemsBo == null || itemsBo.isEmpty()) {
            throw new RuntimeException("出库单明细不能为空");
        }

        List<OutboundOrderItemBo> normalizedItems = new ArrayList<>(itemsBo.size());
        for (int i = 0; i < itemsBo.size(); i++) {
            OutboundOrderItemBo item = itemsBo.get(i);
            if (item == null) {
                continue;
            }
            item.setOrderId(orderId);
            if (item.getLineNo() == null) {
                item.setLineNo(i + 1);
            }
            if (item.getShippedQty() == null) {
                item.setShippedQty(BigDecimal.ZERO);
            }
            if (Objects.isNull(item.getMaterialId())) {
                throw new RuntimeException("出库单明细物料不能为空");
            }
            if (Objects.isNull(item.getPlannedQty())) {
                throw new RuntimeException("出库单明细计划数量不能为空");
            }
            normalizedItems.add(item);
        }
        if (normalizedItems.isEmpty()) {
            throw new RuntimeException("出库单明细不能为空");
        }
        return normalizedItems;
    }

    private void validateOrderItemLocations(Long warehouseId, List<OutboundOrderItemBo> items) {
        if (warehouseId == null || items == null || items.isEmpty()) {
            return;
        }
        Set<Long> locationIds = items.stream()
                .map(OutboundOrderItemBo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (locationIds.isEmpty()) {
            return;
        }
        Map<Long, Location> locationMap = locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
        for (Long locationId : locationIds) {
            Location location = locationMap.get(locationId);
            if (location == null) {
                throw new RuntimeException("出库单明细存在无效库位: " + locationId);
            }
            if (!Objects.equals(location.getWarehouseId(), warehouseId)) {
                throw new RuntimeException("出库单明细库位不属于当前仓库: " + location.getLocationCode());
            }
        }
    }

    private List<StockChangeBo> buildOutboundStockChanges(OutboundOrder order, List<OutboundOrderItem> orderItems) {
        return buildOutboundStockChanges(order, orderItems, LocalDateTime.now());
    }

    private List<StockChangeBo> buildOutboundStockChanges(OutboundOrder order, List<OutboundOrderItem> orderItems,
                                                          LocalDateTime operateTime) {
        List<StockChangeBo> changes = new ArrayList<>(orderItems.size());
        for (OutboundOrderItem item : orderItems) {
            if (item.getLocationId() == null) {
                throw new RuntimeException("出库单明细来源库位不能为空，行号: " + item.getLineNo());
            }
            BigDecimal actualQty = resolveOutboundConfirmedQty(item);
            item.setShippedQty(actualQty);

            StockChangeBo change = new StockChangeBo();
            change.setBizType(StockBizTypeConst.OUTBOUND_ORDER);
            change.setBizId(order.getId());
            change.setWarehouseId(order.getWarehouseId());
            change.setLocationId(item.getLocationId());
            change.setMaterialId(item.getMaterialId());
            change.setLineNo(item.getLineNo());
            change.setChangeQty(actualQty.negate());
            change.setOperateTime(operateTime);
            change.setRemark(StringUtils.defaultIfBlank(item.getRemark(), order.getRemark()));
            changes.add(change);
        }
        return changes;
    }

    private BigDecimal resolveOutboundConfirmedQty(OutboundOrderItem item) {
        BigDecimal actualQty = item.getShippedQty();
        if (actualQty == null || actualQty.compareTo(BigDecimal.ZERO) <= 0) {
            actualQty = item.getPlannedQty();
        }
        if (actualQty == null || actualQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("出库单明细确认数量必须大于0，行号: " + item.getLineNo());
        }
        return actualQty;
    }

    private Boolean confirmOutboundOrder(Long id) {
        OutboundOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在");
        }
        if (!OrderStatusConst.DRAFT.equals(order.getStatus())) {
            if (OrderStatusConst.CONFIRMED.equals(order.getStatus())) {
                throw new RuntimeException("出库单已确认，请勿重复提交");
            }
            throw new RuntimeException("当前状态不可确认");
        }

        LocalDateTime confirmTime = LocalDateTime.now();
        int updatedRows = outboundOrderMapper.confirmDraftOrder(
                id,
                OrderStatusConst.DRAFT,
                OrderStatusConst.CONFIRMED,
                confirmTime,
                resolveOperator()
        );
        if (updatedRows != 1) {
            OutboundOrder latestOrder = getById(id);
            if (latestOrder != null && OrderStatusConst.CONFIRMED.equals(latestOrder.getStatus())) {
                throw new RuntimeException("出库单已确认，请勿重复提交");
            }
            throw new RuntimeException("当前状态不可确认");
        }

        List<OutboundOrderItem> orderItems = outboundOrderItemService.lambdaQuery()
                .eq(OutboundOrderItem::getOrderId, id)
                .list();
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("出库单明细不能为空");
        }

        List<StockChangeBo> stockChanges = buildOutboundStockChanges(order, orderItems, confirmTime);
        inventoryService.applyStockChanges(stockChanges);

        boolean itemsUpdated = outboundOrderItemService.updateBatchById(orderItems);
        if (!itemsUpdated) {
            throw new RuntimeException("出库单明细确认数量更新失败");
        }
        return true;
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

    /**
     * 填充出库单列表的仓库展示字段（编码、名称）。
     *
     * @param records 出库单列表
     */
    private void fillWarehouseDisplay(List<OutboundOrderVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(OutboundOrderVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (warehouseIds.isEmpty()) {
            return;
        }

        Map<Long, Warehouse> warehouseMap = warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));

        for (OutboundOrderVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse == null) {
                continue;
            }
            record.setWarehouseCode(warehouse.getWarehouseCode());
            record.setWarehouseName(warehouse.getWarehouseName());
        }
    }

}





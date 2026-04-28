package com.berlin.aetherflow.wms.service.impl;

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
import com.berlin.aetherflow.wms.domain.bo.InboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderBo;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderItemBo;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.wms.domain.entity.InboundOrderItem;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.InboundOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.InboundOrderVo;
import com.berlin.aetherflow.wms.mapper.InboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InboundOrderItemService;
import com.berlin.aetherflow.wms.service.InventoryService;
import com.berlin.aetherflow.wms.service.InboundOrderService;
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
 * @description 针对表【inbound_order(入库单)】的数据库操作Service实现
 * @createDate 2026-04-15 16:17:27
 */
@Service
@AllArgsConstructor
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder>
        implements InboundOrderService {

    private final InboundOrderMapper inboundOrderMapper;
    private final InboundOrderItemService inboundOrderItemService;
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final InventoryService inventoryService;

    /**
     * 分页查询入库单
     *
     * @param query
     * @return
     */
    @Override
    public PageResult<InboundOrderVo> queryList(InboundOrderQuery query) {
        IPage<InboundOrder> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<InboundOrder> lqw = Wrappers.<InboundOrder>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getOrderNo()), InboundOrder::getOrderNo, query.getOrderNo())
                .eq(query.getWarehouseId() != null, InboundOrder::getWarehouseId, query.getWarehouseId())
                .eq(query.getStatus() != null, InboundOrder::getStatus, query.getStatus())
                .ge(query.getInboundStartTime() != null, InboundOrder::getInboundTime, query.getInboundStartTime())
                .le(query.getInboundEndTime() != null, InboundOrder::getInboundTime, query.getInboundEndTime())
                .like(StringUtils.isNotBlank(query.getRemark()), InboundOrder::getRemark, query.getRemark());
        if (query.getAreaId() != null) {
            lqw.inSql(InboundOrder::getId,
                    "select distinct i.order_id from inbound_order_item i " +
                            "join location l on i.location_id = l.id " +
                            "where l.area_id = " + query.getAreaId());
        }

        IPage<InboundOrder> result = inboundOrderMapper.selectPage(page, lqw);
        List<InboundOrderVo> records = result.getRecords().stream()
                .map(e -> MapstructUtils.convert(e, InboundOrderVo.class))
                .toList();
        fillWarehouseDisplay(records);

        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    /**
     * 暂存入库单
     *
     * @param bo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInboundOrder(InboundOrderBo bo) {
        // 生成入库单号
        bo.setOrderNo(CodeGenerate.generateSimple(BizCodeTypeConst.INBOUND_ORDER));
        InboundOrder order = MapstructUtils.convert(bo, InboundOrder.class);
        inboundOrderMapper.insert(order);

        // 生成入库单详情
        List<InboundOrderItemBo> itemsBo = normalizeOrderItems(order.getId(), bo.getOrderItemsBo());
        validateOrderItemLocations(order.getWarehouseId(), itemsBo);
        inboundOrderItemService.saveInboundOrderItems(itemsBo);

        return order.getId();
    }

    /**
     * 编辑入库单（状态！=完成）
     *
     * @param bo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateInboundOrder(InboundOrderBo bo) {
        InboundOrder order = getById(bo.getId());
        if (order == null) {
            throw new RuntimeException("入库单不存在");
        }
        if (OrderStatusConst.CONFIRMED.equals(order.getStatus())) {
            throw new RuntimeException("已确认单据不允许编辑");
        }

        InboundOrder toUpdate = MapstructUtils.convert(bo, InboundOrder.class);
        boolean updated = updateById(toUpdate);
        if (!updated) {
            throw new RuntimeException("入库单更新失败");
        }

        if (bo.getOrderItemsBo() != null) {
            List<InboundOrderItemBo> normalizedItems = normalizeOrderItems(bo.getId(), bo.getOrderItemsBo());
            Long warehouseIdForValidation = toUpdate.getWarehouseId() != null ? toUpdate.getWarehouseId() : order.getWarehouseId();
            validateOrderItemLocations(warehouseIdForValidation, normalizedItems);
            inboundOrderItemService.replaceInboundOrderItems(bo.getId(), normalizedItems);
        }
        return true;
    }

    /**
     * 状态更新
     *
     * @param id
     * @param bo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, InboundOrderActionBo bo) {
        InboundOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("入库单不存在");
        }

        String action = bo.getAction();
        if (StringUtils.isBlank(action)) {
            throw new RuntimeException("动作不能为空");
        }
        action = action.trim().toUpperCase(Locale.ROOT);
        Integer current = order.getStatus();

        if ("CONFIRM".equals(action)) {
            if (!OrderStatusConst.DRAFT.equals(current)) {
                throw new RuntimeException("当前状态不可确认");
            }
            List<InboundOrderItem> orderItems = inboundOrderItemService.lambdaQuery()
                    .eq(InboundOrderItem::getOrderId, id)
                    .list();
            if (orderItems == null || orderItems.isEmpty()) {
                throw new RuntimeException("入库单明细不能为空");
            }

            List<StockChangeBo> stockChanges = buildInboundStockChanges(order, orderItems);
            inventoryService.applyStockChanges(stockChanges);

            boolean itemsUpdated = inboundOrderItemService.updateBatchById(orderItems);
            if (!itemsUpdated) {
                throw new RuntimeException("入库单明细确认数量更新失败");
            }

            order.setStatus(OrderStatusConst.CONFIRMED);
            if (order.getInboundTime() == null) {
                order.setInboundTime(LocalDateTime.now());
            }
            boolean ok = updateById(order);
            if (!ok) {
                throw new RuntimeException("状态更新失败");
            }
            return true;
        }

        throw new RuntimeException("不支持的动作: " + action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeInboundOrders(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        List<InboundOrder> orders = lambdaQuery()
                .in(InboundOrder::getId, ids)
                .list();
        if (orders.isEmpty()) {
            return true;
        }

        InboundOrder confirmedOrder = orders.stream()
                .filter(order -> OrderStatusConst.CONFIRMED.equals(order.getStatus()))
                .findFirst()
                .orElse(null);
        if (confirmedOrder != null) {
            throw new RuntimeException("已确认入库单不允许删除: " + confirmedOrder.getOrderNo());
        }

        List<Long> orderIds = orders.stream()
                .map(InboundOrder::getId)
                .toList();
        inboundOrderItemService.remove(Wrappers.<InboundOrderItem>lambdaQuery()
                .in(InboundOrderItem::getOrderId, orderIds));
        return removeByIds(orderIds);
    }

    /**
     * 标准化明细：补全 orderId、lineNo、receivedQty。
     */
    private List<InboundOrderItemBo> normalizeOrderItems(Long orderId, List<InboundOrderItemBo> itemsBo) {
        if (itemsBo == null || itemsBo.isEmpty()) {
            throw new RuntimeException("入库单明细不能为空");
        }

        List<InboundOrderItemBo> normalizedItems = new ArrayList<>(itemsBo.size());
        for (int i = 0; i < itemsBo.size(); i++) {
            InboundOrderItemBo item = itemsBo.get(i);
            if (item == null) {
                continue;
            }
            item.setOrderId(orderId);
            if (item.getLineNo() == null) {
                item.setLineNo(i + 1);
            }
            if (item.getReceivedQty() == null) {
                item.setReceivedQty(BigDecimal.ZERO);
            }
            if (Objects.isNull(item.getMaterialId())) {
                throw new RuntimeException("入库单明细物料不能为空");
            }
            if (Objects.isNull(item.getPlannedQty())) {
                throw new RuntimeException("入库单明细计划数量不能为空");
            }
            normalizedItems.add(item);
        }
        if (normalizedItems.isEmpty()) {
            throw new RuntimeException("入库单明细不能为空");
        }
        return normalizedItems;
    }

    private void validateOrderItemLocations(Long warehouseId, List<InboundOrderItemBo> items) {
        if (warehouseId == null || items == null || items.isEmpty()) {
            return;
        }
        Set<Long> locationIds = items.stream()
                .map(InboundOrderItemBo::getLocationId)
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
                throw new RuntimeException("入库单明细存在无效库位: " + locationId);
            }
            if (!Objects.equals(location.getWarehouseId(), warehouseId)) {
                throw new RuntimeException("入库单明细库位不属于当前仓库: " + location.getLocationCode());
            }
        }
    }

    private List<StockChangeBo> buildInboundStockChanges(InboundOrder order, List<InboundOrderItem> orderItems) {
        List<StockChangeBo> changes = new ArrayList<>(orderItems.size());
        for (InboundOrderItem item : orderItems) {
            if (item.getLocationId() == null) {
                throw new RuntimeException("入库单明细目标库位不能为空，行号: " + item.getLineNo());
            }
            BigDecimal actualQty = resolveInboundConfirmedQty(item);
            item.setReceivedQty(actualQty);

            StockChangeBo change = new StockChangeBo();
            change.setBizType(StockBizTypeConst.INBOUND_ORDER);
            change.setBizId(order.getId());
            change.setWarehouseId(order.getWarehouseId());
            change.setLocationId(item.getLocationId());
            change.setMaterialId(item.getMaterialId());
            change.setChangeQty(actualQty);
            change.setOperateTime(LocalDateTime.now());
            change.setRemark(StringUtils.defaultIfBlank(item.getRemark(), order.getRemark()));
            changes.add(change);
        }
        return changes;
    }

    private BigDecimal resolveInboundConfirmedQty(InboundOrderItem item) {
        BigDecimal actualQty = item.getReceivedQty();
        if (actualQty == null || actualQty.compareTo(BigDecimal.ZERO) <= 0) {
            actualQty = item.getPlannedQty();
        }
        if (actualQty == null || actualQty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("入库单明细确认数量必须大于0，行号: " + item.getLineNo());
        }
        return actualQty;
    }

    /**
     * 填充入库单列表的仓库展示字段（编码、名称）。
     *
     * @param records 入库单列表
     */
    private void fillWarehouseDisplay(List<InboundOrderVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(InboundOrderVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (warehouseIds.isEmpty()) {
            return;
        }

        Map<Long, Warehouse> warehouseMap = warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));

        for (InboundOrderVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse == null) {
                continue;
            }
            record.setWarehouseCode(warehouse.getWarehouseCode());
            record.setWarehouseName(warehouse.getWarehouseName());
        }
    }
}

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
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderItemBo;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.OutboundOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.OutboundOrderVo;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.OutboundOrderItemService;
import com.berlin.aetherflow.wms.service.OutboundOrderService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        OutboundOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("出库单不存在");
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
            order.setStatus(OrderStatusConst.CONFIRMED);
            boolean ok = updateById(order);
            if (!ok) {
                throw new RuntimeException("状态更新失败");
            }
            // TODO 确认前校验库存充足 + 确认后扣减库存 + 记录状态流转日志
            return true;
        }

        throw new RuntimeException("不支持的动作: " + action);
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





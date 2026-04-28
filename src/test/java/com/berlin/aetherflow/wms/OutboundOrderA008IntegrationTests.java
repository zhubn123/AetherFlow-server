package com.berlin.aetherflow.wms;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderItemBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import com.berlin.aetherflow.wms.domain.entity.StockTransaction;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderItemMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.StockTransactionMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.OutboundOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OutboundOrderA008IntegrationTests {

    @Autowired
    private OutboundOrderService outboundOrderService;

    @Autowired
    private WarehouseMapper warehouseMapper;

    @Autowired
    private AreaMapper areaMapper;

    @Autowired
    private LocationMapper locationMapper;

    @Autowired
    private MaterialMapper materialMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private OutboundOrderMapper outboundOrderMapper;

    @Autowired
    private OutboundOrderItemMapper outboundOrderItemMapper;

    @Autowired
    private StockTransactionMapper stockTransactionMapper;

    private TestFixture fixture;

    @AfterEach
    void cleanupFixture() {
        if (fixture == null) {
            return;
        }
        stockTransactionMapper.delete(Wrappers.<StockTransaction>lambdaQuery()
                .eq(StockTransaction::getBizType, StockBizTypeConst.OUTBOUND_ORDER)
                .eq(StockTransaction::getBizId, fixture.orderId()));
        outboundOrderItemMapper.delete(Wrappers.<OutboundOrderItem>lambdaQuery()
                .eq(OutboundOrderItem::getOrderId, fixture.orderId()));
        outboundOrderMapper.deleteById(fixture.orderId());
        inventoryMapper.deleteById(fixture.inventoryId());
        locationMapper.deleteById(fixture.locationId());
        areaMapper.deleteById(fixture.areaId());
        materialMapper.deleteById(fixture.materialId());
        warehouseMapper.deleteById(fixture.warehouseId());
        fixture = null;
    }

    @Test
    void confirmOutboundOrderShouldDeductInventoryAndWriteTransaction() {
        fixture = createFixture(new BigDecimal("20.00"), new BigDecimal("5.00"));

        outboundOrderService.applyAction(fixture.orderId(), confirmAction());

        OutboundOrder order = outboundOrderMapper.selectById(fixture.orderId());
        assertEquals(OrderStatusConst.CONFIRMED, order.getStatus());
        assertNotNull(order.getOutboundTime());

        Inventory inventory = inventoryMapper.selectById(fixture.inventoryId());
        assertBigDecimalEquals("15.00", inventory.getQuantity());

        OutboundOrderItem item = outboundOrderItemMapper.selectOne(Wrappers.<OutboundOrderItem>lambdaQuery()
                .eq(OutboundOrderItem::getOrderId, fixture.orderId())
                .eq(OutboundOrderItem::getLineNo, 1));
        assertNotNull(item);
        assertBigDecimalEquals("5.00", item.getShippedQty());

        List<StockTransaction> transactions = stockTransactionMapper.selectList(Wrappers.<StockTransaction>lambdaQuery()
                .eq(StockTransaction::getBizType, StockBizTypeConst.OUTBOUND_ORDER)
                .eq(StockTransaction::getBizId, fixture.orderId()));
        assertEquals(1, transactions.size());
        assertBigDecimalEquals("-5.00", transactions.getFirst().getChangeQty());
        assertBigDecimalEquals("20.00", transactions.getFirst().getBeforeQty());
        assertBigDecimalEquals("15.00", transactions.getFirst().getAfterQty());
    }

    @Test
    void confirmOutboundOrderShouldFailWhenInventoryIsInsufficient() {
        fixture = createFixture(new BigDecimal("3.00"), new BigDecimal("5.00"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> outboundOrderService.applyAction(fixture.orderId(), confirmAction()));
        assertTrue(exception.getMessage().contains("库存不足"));

        OutboundOrder order = outboundOrderMapper.selectById(fixture.orderId());
        assertEquals(OrderStatusConst.DRAFT, order.getStatus());

        Inventory inventory = inventoryMapper.selectById(fixture.inventoryId());
        assertBigDecimalEquals("3.00", inventory.getQuantity());

        OutboundOrderItem item = outboundOrderItemMapper.selectOne(Wrappers.<OutboundOrderItem>lambdaQuery()
                .eq(OutboundOrderItem::getOrderId, fixture.orderId())
                .eq(OutboundOrderItem::getLineNo, 1));
        assertNotNull(item);
        assertBigDecimalEquals("0.00", item.getShippedQty());

        Long transactionCount = stockTransactionMapper.selectCount(Wrappers.<StockTransaction>lambdaQuery()
                .eq(StockTransaction::getBizType, StockBizTypeConst.OUTBOUND_ORDER)
                .eq(StockTransaction::getBizId, fixture.orderId()));
        assertEquals(0L, transactionCount);
    }

    @Test
    void concurrentConfirmShouldOnlyDeductOnce() throws Exception {
        fixture = createFixture(new BigDecimal("10.00"), new BigDecimal("4.00"));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Callable<ActionResult> task = () -> {
                ready.countDown();
                assertTrue(start.await(5, TimeUnit.SECONDS));
                try {
                    boolean result = outboundOrderService.applyAction(fixture.orderId(), confirmAction());
                    return ActionResult.success(result);
                } catch (Exception ex) {
                    return ActionResult.failure(ex.getMessage());
                }
            };

            Future<ActionResult> first = executorService.submit(task);
            Future<ActionResult> second = executorService.submit(task);

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            ActionResult firstResult = first.get(10, TimeUnit.SECONDS);
            ActionResult secondResult = second.get(10, TimeUnit.SECONDS);

            long successCount = List.of(firstResult, secondResult).stream()
                    .filter(ActionResult::success)
                    .count();
            long failureCount = 2 - successCount;

            assertEquals(1L, successCount);
            assertEquals(1L, failureCount);
            assertTrue(List.of(firstResult, secondResult).stream()
                    .filter(result -> !result.success())
                    .map(ActionResult::message)
                    .anyMatch(message -> message != null
                            && (message.contains("重复提交") || message.contains("处理中"))));

            Inventory inventory = inventoryMapper.selectById(fixture.inventoryId());
            assertBigDecimalEquals("6.00", inventory.getQuantity());

            OutboundOrder order = outboundOrderMapper.selectById(fixture.orderId());
            assertEquals(OrderStatusConst.CONFIRMED, order.getStatus());

            Long transactionCount = stockTransactionMapper.selectCount(Wrappers.<StockTransaction>lambdaQuery()
                    .eq(StockTransaction::getBizType, StockBizTypeConst.OUTBOUND_ORDER)
                    .eq(StockTransaction::getBizId, fixture.orderId()));
            assertEquals(1L, transactionCount);
        } finally {
            executorService.shutdownNow();
        }
    }

    private TestFixture createFixture(BigDecimal inventoryQty, BigDecimal plannedQty) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode("TWH-" + suffix);
        warehouse.setWarehouseName("测试仓库-" + suffix);
        warehouse.setStatus(0);
        warehouse.setRemark("A008 test");
        warehouseMapper.insert(warehouse);

        Area area = new Area();
        area.setWarehouseId(warehouse.getId());
        area.setAreaCode("TA-" + suffix);
        area.setAreaName("测试区域-" + suffix);
        area.setAreaType("PICK");
        area.setStatus(0);
        area.setRemark("A008 test");
        areaMapper.insert(area);

        Location location = new Location();
        location.setWarehouseId(warehouse.getId());
        location.setAreaId(area.getId());
        location.setLocationCode("TL-" + suffix);
        location.setLocationName("测试库位-" + suffix);
        location.setStatus(0);
        location.setRemark("A008 test");
        locationMapper.insert(location);

        Material material = new Material();
        material.setMaterialCode("TM-" + suffix);
        material.setMaterialName("测试物料-" + suffix);
        material.setSpecification("SPEC-" + suffix);
        material.setUnit("EA");
        material.setStatus(0);
        material.setRemark("A008 test");
        materialMapper.insert(material);

        Inventory inventory = new Inventory();
        inventory.setWarehouseId(warehouse.getId());
        inventory.setLocationId(location.getId());
        inventory.setMaterialId(material.getId());
        inventory.setQuantity(inventoryQty);
        inventory.setLockedQuantity(BigDecimal.ZERO);
        inventoryMapper.insert(inventory);

        OutboundOrderItemBo itemBo = new OutboundOrderItemBo();
        itemBo.setLineNo(1);
        itemBo.setMaterialId(material.getId());
        itemBo.setLocationId(location.getId());
        itemBo.setPlannedQty(plannedQty);
        itemBo.setRemark("A008 test");

        OutboundOrderBo orderBo = new OutboundOrderBo();
        orderBo.setWarehouseId(warehouse.getId());
        orderBo.setRemark("A008 test");
        orderBo.setOrderItemsBo(List.of(itemBo));

        Long orderId = outboundOrderService.createOutboundOrder(orderBo);
        return new TestFixture(
                warehouse.getId(),
                area.getId(),
                location.getId(),
                material.getId(),
                inventory.getId(),
                orderId
        );
    }

    private OutboundOrderActionBo confirmAction() {
        OutboundOrderActionBo actionBo = new OutboundOrderActionBo();
        actionBo.setAction("CONFIRM");
        return actionBo;
    }

    private void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }

    private record TestFixture(Long warehouseId, Long areaId, Long locationId, Long materialId,
                               Long inventoryId, Long orderId) {
    }

    private record ActionResult(boolean success, boolean payload, String message) {

        static ActionResult success(boolean payload) {
            return new ActionResult(true, payload, null);
        }

        static ActionResult failure(String message) {
            return new ActionResult(false, false, message);
        }
    }
}

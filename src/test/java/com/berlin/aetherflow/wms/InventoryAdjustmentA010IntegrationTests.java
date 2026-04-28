package com.berlin.aetherflow.wms;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.berlin.aetherflow.wms.constant.InventoryAdjustmentTypeConst;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentActionBo;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentBo;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentItemBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustmentItem;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.StockTransaction;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.InventoryAdjustmentItemMapper;
import com.berlin.aetherflow.wms.mapper.InventoryAdjustmentMapper;
import com.berlin.aetherflow.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.StockTransactionMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InventoryAdjustmentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventoryAdjustmentA010IntegrationTests {

    @Autowired
    private InventoryAdjustmentService inventoryAdjustmentService;

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
    private InventoryAdjustmentMapper inventoryAdjustmentMapper;

    @Autowired
    private InventoryAdjustmentItemMapper inventoryAdjustmentItemMapper;

    @Autowired
    private StockTransactionMapper stockTransactionMapper;

    private TestFixture fixture;

    @AfterEach
    void cleanupFixture() {
        if (fixture == null) {
            return;
        }
        stockTransactionMapper.delete(Wrappers.<StockTransaction>lambdaQuery()
                .eq(StockTransaction::getBizType, StockBizTypeConst.INVENTORY_ADJUSTMENT)
                .eq(StockTransaction::getBizId, fixture.orderId()));
        inventoryAdjustmentItemMapper.delete(Wrappers.<InventoryAdjustmentItem>lambdaQuery()
                .eq(InventoryAdjustmentItem::getOrderId, fixture.orderId()));
        inventoryAdjustmentMapper.deleteById(fixture.orderId());
        inventoryMapper.deleteById(fixture.inventoryId());
        locationMapper.deleteById(fixture.locationId());
        areaMapper.deleteById(fixture.areaId());
        materialMapper.deleteById(fixture.materialId());
        warehouseMapper.deleteById(fixture.warehouseId());
        fixture = null;
    }

    @Test
    void confirmIncreaseAdjustmentShouldUpdateInventoryAndWriteTransaction() {
        fixture = createFixture(InventoryAdjustmentTypeConst.INCREASE, new BigDecimal("5.00"), new BigDecimal("3.00"));

        inventoryAdjustmentService.applyAction(fixture.orderId(), confirmAction());

        InventoryAdjustment order = inventoryAdjustmentMapper.selectById(fixture.orderId());
        assertEquals(OrderStatusConst.CONFIRMED, order.getStatus());
        assertNotNull(order.getAdjustTime());

        Inventory inventory = inventoryMapper.selectById(fixture.inventoryId());
        assertBigDecimalEquals("8.00", inventory.getQuantity());

        List<StockTransaction> transactions = stockTransactionMapper.selectList(Wrappers.<StockTransaction>lambdaQuery()
                .eq(StockTransaction::getBizType, StockBizTypeConst.INVENTORY_ADJUSTMENT)
                .eq(StockTransaction::getBizId, fixture.orderId()));
        assertEquals(1, transactions.size());
        assertBigDecimalEquals("3.00", transactions.getFirst().getChangeQty());
        assertBigDecimalEquals("5.00", transactions.getFirst().getBeforeQty());
        assertBigDecimalEquals("8.00", transactions.getFirst().getAfterQty());
        assertTrue(transactions.getFirst().getRemark().contains("A010原因"));
    }

    @Test
    void confirmDecreaseAdjustmentShouldFailWhenInventoryIsInsufficient() {
        fixture = createFixture(InventoryAdjustmentTypeConst.DECREASE, new BigDecimal("2.00"), new BigDecimal("5.00"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryAdjustmentService.applyAction(fixture.orderId(), confirmAction()));
        assertTrue(exception.getMessage().contains("库存不足"));

        InventoryAdjustment order = inventoryAdjustmentMapper.selectById(fixture.orderId());
        assertEquals(OrderStatusConst.DRAFT, order.getStatus());

        Inventory inventory = inventoryMapper.selectById(fixture.inventoryId());
        assertBigDecimalEquals("2.00", inventory.getQuantity());

        Long transactionCount = stockTransactionMapper.selectCount(Wrappers.<StockTransaction>lambdaQuery()
                .eq(StockTransaction::getBizType, StockBizTypeConst.INVENTORY_ADJUSTMENT)
                .eq(StockTransaction::getBizId, fixture.orderId()));
        assertEquals(0L, transactionCount);
    }

    @Test
    void confirmedAdjustmentShouldNotAllowDelete() {
        fixture = createFixture(InventoryAdjustmentTypeConst.INCREASE, new BigDecimal("1.00"), new BigDecimal("1.00"));
        inventoryAdjustmentService.applyAction(fixture.orderId(), confirmAction());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryAdjustmentService.removeInventoryAdjustments(List.of(fixture.orderId())));
        assertTrue(exception.getMessage().contains("已确认库存调整单不允许删除"));
    }

    private TestFixture createFixture(String adjustType, BigDecimal inventoryQty, BigDecimal adjustQty) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Warehouse warehouse = new Warehouse();
        warehouse.setWarehouseCode("A10-WH-" + suffix);
        warehouse.setWarehouseName("A010测试仓库-" + suffix);
        warehouse.setStatus(0);
        warehouse.setRemark("A010 test");
        warehouseMapper.insert(warehouse);

        Area area = new Area();
        area.setWarehouseId(warehouse.getId());
        area.setAreaCode("A10-AR-" + suffix);
        area.setAreaName("A010测试区域-" + suffix);
        area.setAreaType("STORAGE");
        area.setStatus(0);
        area.setRemark("A010 test");
        areaMapper.insert(area);

        Location location = new Location();
        location.setWarehouseId(warehouse.getId());
        location.setAreaId(area.getId());
        location.setLocationCode("A10-LC-" + suffix);
        location.setLocationName("A010测试库位-" + suffix);
        location.setStatus(0);
        location.setRemark("A010 test");
        locationMapper.insert(location);

        Material material = new Material();
        material.setMaterialCode("A10-MT-" + suffix);
        material.setMaterialName("A010测试物料-" + suffix);
        material.setSpecification("SPEC-" + suffix);
        material.setUnit("EA");
        material.setStatus(0);
        material.setRemark("A010 test");
        materialMapper.insert(material);

        Inventory inventory = new Inventory();
        inventory.setWarehouseId(warehouse.getId());
        inventory.setLocationId(location.getId());
        inventory.setMaterialId(material.getId());
        inventory.setQuantity(inventoryQty);
        inventory.setLockedQuantity(BigDecimal.ZERO);
        inventoryMapper.insert(inventory);

        InventoryAdjustmentItemBo itemBo = new InventoryAdjustmentItemBo();
        itemBo.setLineNo(1);
        itemBo.setMaterialId(material.getId());
        itemBo.setLocationId(location.getId());
        itemBo.setAdjustQty(adjustQty);
        itemBo.setRemark("A010明细备注");

        InventoryAdjustmentBo orderBo = new InventoryAdjustmentBo();
        orderBo.setWarehouseId(warehouse.getId());
        orderBo.setAreaId(area.getId());
        orderBo.setAdjustType(adjustType);
        orderBo.setAdjustReason("A010原因-" + suffix);
        orderBo.setStatus(OrderStatusConst.DRAFT);
        orderBo.setRemark("A010备注");
        orderBo.setAdjustmentItemsBo(List.of(itemBo));

        Long orderId = inventoryAdjustmentService.createInventoryAdjustment(orderBo);
        return new TestFixture(
                warehouse.getId(),
                area.getId(),
                location.getId(),
                material.getId(),
                inventory.getId(),
                orderId
        );
    }

    private InventoryAdjustmentActionBo confirmAction() {
        InventoryAdjustmentActionBo actionBo = new InventoryAdjustmentActionBo();
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
}

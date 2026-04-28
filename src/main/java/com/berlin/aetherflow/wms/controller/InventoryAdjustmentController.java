package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentActionBo;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentBo;
import com.berlin.aetherflow.wms.domain.query.InventoryAdjustmentQuery;
import com.berlin.aetherflow.wms.service.InventoryAdjustmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存调整单 Controller。
 */
@RestController
@RequestMapping("/api/wms/inventory-adjustments")
@AllArgsConstructor
public class InventoryAdjustmentController {

    private final InventoryAdjustmentService inventoryAdjustmentService;

    @Operation(summary = "库存调整单详情查询")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(inventoryAdjustmentService.getDetailById(id));
    }

    @Operation(summary = "库存调整单分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject InventoryAdjustmentQuery query) {
        return Result.success(inventoryAdjustmentService.queryList(query));
    }

    @Operation(summary = "创建库存调整单草稿")
    @PostMapping
    public Result<?> createDraft(@Validated(CreateGroup.class) @RequestBody InventoryAdjustmentBo bo) {
        bo.setId(null);
        bo.setStatus(OrderStatusConst.DRAFT);
        return Result.success(inventoryAdjustmentService.createInventoryAdjustment(bo));
    }

    @Operation(summary = "编辑库存调整单")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody InventoryAdjustmentBo bo) {
        bo.setId(id);
        return Result.success(inventoryAdjustmentService.updateInventoryAdjustment(bo));
    }

    @Operation(summary = "执行库存调整单动作")
    @PostMapping("/{id}/actions")
    public Result<?> applyAction(@PathVariable Long id, @Valid @RequestBody InventoryAdjustmentActionBo bo) {
        return Result.success(inventoryAdjustmentService.applyAction(id, bo));
    }

    @Operation(summary = "批量删除库存调整单")
    @DeleteMapping
    public Result<?> removeBatch(@RequestParam List<Long> ids) {
        return Result.success(inventoryAdjustmentService.removeInventoryAdjustments(ids));
    }
}

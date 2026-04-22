package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.wms.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存 Controller。
 *
 * @author zhubn
 * @date 2026/4/15
 */
@RestController
@RequestMapping("/api/wms/stocks")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "根据ID查询库存")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }

    @Operation(summary = "库存分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject InventoryQuery query) {
        return Result.success(inventoryService.queryList(query));
    }
}

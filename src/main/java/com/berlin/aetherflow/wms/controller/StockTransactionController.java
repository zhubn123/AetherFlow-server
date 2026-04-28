package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.query.StockTransactionQuery;
import com.berlin.aetherflow.wms.service.StockTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存流水 Controller。
 */
@RestController
@RequestMapping("/api/wms/stock-transactions")
@AllArgsConstructor
public class StockTransactionController {

    private final StockTransactionService stockTransactionService;

    @Operation(summary = "分页查询库存流水")
    @GetMapping
    public Result<?> page(@ParameterObject StockTransactionQuery query) {
        return Result.success(stockTransactionService.queryList(query));
    }

    @Operation(summary = "查询库存流水详情")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(stockTransactionService.getDetailById(id));
    }
}

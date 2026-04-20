package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderBo;
import com.berlin.aetherflow.wms.domain.query.InboundOrderQuery;
import com.berlin.aetherflow.wms.service.InboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入库单 Controller。
 *
 * @author zhubn
 * @date 2026/4/15
 */
@RestController
@RequestMapping("/api/wms/inbound-orders")
@AllArgsConstructor
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    @Operation(summary = "根据id查入库单")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(inboundOrderService.getById(id));
    }

    @Operation(summary = "入库单分页查询")
    @PostMapping("/page")
    public Result<?> list(@RequestBody InboundOrderQuery query) {
        return Result.success(inboundOrderService.queryList(query));
    }

    // TODO 增删改
    @Operation(summary = "入库单创建")
    @PostMapping
    public Result<?> create(@RequestBody InboundOrderBo bo) {
        bo.setId(null);
        return Result.success(inboundOrderService.createInboundOrder(bo));
    }

    @Operation(summary = "入库单修改")
    @PutMapping
    public Result<?> update(@RequestBody InboundOrderQuery query) {
        return Result.success(inboundOrderService.queryList(query));
    }

    @Operation(summary = "入库单删除")
    @DeleteMapping
    public Result<?> delete(@RequestParam List<Long> ids) {
        return Result.success(inboundOrderService.removeByIds(ids));
    }
}

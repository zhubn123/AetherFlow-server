package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderBo;
import com.berlin.aetherflow.wms.domain.query.InboundOrderQuery;
import com.berlin.aetherflow.wms.service.InboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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

    @Operation(summary = "根据ID查询入库单")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(inboundOrderService.getById(id));
    }

    @Operation(summary = "入库单分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject InboundOrderQuery query) {
        return Result.success(inboundOrderService.queryList(query));
    }

    // 入库单采用两步制：
    // 1) 暂存(draft)：仅保存单据和明细，不改库存；
    // 2) 确认(confirmed)：校验规则后一次性入账库存并记录流转日志。
    @Operation(summary = "创建入库单草稿")
    @PostMapping
    public Result<?> createDraft(@RequestBody InboundOrderBo bo) {
        bo.setId(null);
        bo.setStatus(OrderStatusConst.DRAFT);
        return Result.success(inboundOrderService.createInboundOrder(bo));
    }

    @Operation(summary = "编辑入库单")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody InboundOrderBo bo) {
        bo.setId(id);
        return Result.success(inboundOrderService.updateInboundOrder(bo));
    }

    @Operation(summary = "执行入库单动作")
    @PostMapping("/{id}/actions")
    public Result<?> applyAction(@PathVariable Long id, @RequestBody InboundOrderActionBo bo) {
        return Result.success(inboundOrderService.applyAction(id, bo));
    }

    @Operation(summary = "批量删除入库单")
    @DeleteMapping
    public Result<?> removeBatch(@RequestParam List<Long> ids) {
        return Result.success(inboundOrderService.removeByIds(ids));
    }
}

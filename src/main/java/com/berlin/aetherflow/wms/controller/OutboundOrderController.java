package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.OutboundOrderBo;
import com.berlin.aetherflow.wms.domain.query.OutboundOrderQuery;
import jakarta.validation.Valid;
import com.berlin.aetherflow.wms.service.OutboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

/**
 * 出库单 Controller。
 *
 * @author zhubn
 * @date 2026/4/15
 */
@RestController
@RequestMapping("/api/wms/outbound-orders")
@AllArgsConstructor
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    @Operation(summary = "根据ID查询出库单")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(outboundOrderService.getById(id));
    }

    @Operation(summary = "出库单分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject OutboundOrderQuery query) {
        return Result.success(outboundOrderService.queryList(query));
    }

    // 出库单采用两步制：
    // 1) 暂存(draft)：仅保存单据和明细，不改库存；
    // 2) 确认(confirmed)：校验库存后一次性扣减库存并记录流转日志。
    @Operation(summary = "创建出库单草稿")
    @PostMapping
    public Result<?> createDraft(@Validated(CreateGroup.class) @RequestBody OutboundOrderBo bo) {
        bo.setId(null);
        bo.setStatus(OrderStatusConst.DRAFT);
        return Result.success(outboundOrderService.createOutboundOrder(bo));
    }

    @Operation(summary = "编辑出库单")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody OutboundOrderBo bo) {
        bo.setId(id);
        return Result.success(outboundOrderService.updateOutboundOrder(bo));
    }

    @Operation(summary = "执行出库单动作")
    @PostMapping("/{id}/actions")
    public Result<?> applyAction(@PathVariable Long id, @Valid @RequestBody OutboundOrderActionBo bo) {
        return Result.success(outboundOrderService.applyAction(id, bo));
    }

    @Operation(summary = "批量删除出库单")
    @DeleteMapping
    public Result<?> removeBatch(@RequestParam List<Long> ids) {
        return Result.success(outboundOrderService.removeOutboundOrders(ids));
    }
}

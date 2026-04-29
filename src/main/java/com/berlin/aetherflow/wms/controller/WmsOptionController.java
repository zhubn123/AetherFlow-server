package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.query.WmsOptionQuery;
import com.berlin.aetherflow.wms.service.WmsOptionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMS 主数据选项接口。
 */
@RestController
@RequestMapping("/api/wms/options")
@AllArgsConstructor
public class WmsOptionController {

    private final WmsOptionService wmsOptionService;

    @Operation(summary = "仓库选项查询")
    @GetMapping("/warehouses")
    public Result<?> warehouses(@ParameterObject WmsOptionQuery query) {
        return Result.success(wmsOptionService.queryWarehouseOptions(query));
    }

    @Operation(summary = "区域选项查询")
    @GetMapping("/areas")
    public Result<?> areas(@ParameterObject WmsOptionQuery query) {
        return Result.success(wmsOptionService.queryAreaOptions(query));
    }

    @Operation(summary = "库位选项查询")
    @GetMapping("/locations")
    public Result<?> locations(@ParameterObject WmsOptionQuery query) {
        return Result.success(wmsOptionService.queryLocationOptions(query));
    }

    @Operation(summary = "物料选项查询")
    @GetMapping("/materials")
    public Result<?> materials(@ParameterObject WmsOptionQuery query) {
        return Result.success(wmsOptionService.queryMaterialOptions(query));
    }
}

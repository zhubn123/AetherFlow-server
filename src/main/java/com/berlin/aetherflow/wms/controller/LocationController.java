package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.LocationBo;
import com.berlin.aetherflow.wms.domain.query.LocationQuery;
import com.berlin.aetherflow.wms.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库位 Controller。
 *
 * @author zhubn
 * @date 2026/4/15
 */
@RestController
@RequestMapping("/api/wms/locations")
@AllArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "根据ID查询库位")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(locationService.getById(id));
    }

    @Operation(summary = "库位分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject LocationQuery query) {
        return Result.success(locationService.queryList(query));
    }

    @Operation(summary = "创建库位")
    @PostMapping
    public Result<?> create(@RequestBody LocationBo bo) {
        bo.setId(null);
        return Result.success(locationService.createLocation(bo));
    }

    @Operation(summary = "编辑库位")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody LocationBo bo) {
        bo.setId(id);
        return Result.success(locationService.updateLocation(bo));
    }

    @Operation(summary = "批量删除库位")
    @DeleteMapping
    public Result<?> removeBatch(@RequestParam List<Long> ids) {
        return Result.success(locationService.removeLocations(ids));
    }
}

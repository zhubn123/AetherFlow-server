package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.AreaBo;
import com.berlin.aetherflow.wms.domain.query.AreaQuery;
import com.berlin.aetherflow.wms.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
 * 区域 Controller。
 */
@RestController
@RequestMapping("/api/wms/areas")
@AllArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @Operation(summary = "根据ID查询区域")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(areaService.getById(id));
    }

    @Operation(summary = "区域分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject AreaQuery query) {
        return Result.success(areaService.queryList(query));
    }

    @Operation(summary = "创建区域")
    @PostMapping
    public Result<?> create(@RequestBody AreaBo bo) {
        bo.setId(null);
        return Result.success(areaService.createArea(bo));
    }

    @Operation(summary = "编辑区域")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody AreaBo bo) {
        bo.setId(id);
        return Result.success(areaService.updateArea(bo));
    }

    @Operation(summary = "批量删除区域")
    @DeleteMapping
    public Result<?> removeBatch(@RequestParam List<Long> ids) {
        return Result.success(areaService.removeAreas(ids));
    }
}

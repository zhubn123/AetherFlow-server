package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.MaterialBo;
import com.berlin.aetherflow.wms.domain.query.MaterialQuery;
import com.berlin.aetherflow.wms.service.MaterialService;
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
 * 物料 Controller。
 *
 * @author zhubn
 * @date 2026/4/15
 */
@RestController
@RequestMapping("/api/wms/materials")
@AllArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "根据ID查询物料")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(materialService.getById(id));
    }

    @Operation(summary = "物料分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject MaterialQuery query) {
        return Result.success(materialService.queryList(query));
    }

    @Operation(summary = "创建物料")
    @PostMapping
    public Result<?> create(@RequestBody MaterialBo bo) {
        bo.setId(null);
        return Result.success(materialService.createMaterial(bo));
    }

    @Operation(summary = "编辑物料")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody MaterialBo bo) {
        bo.setId(id);
        return Result.success(materialService.updateMaterial(bo));
    }

    @Operation(summary = "批量删除物料")
    @DeleteMapping
    public Result<?> removeBatch(@RequestParam List<Long> ids) {
        return Result.success(materialService.removeMaterials(ids));
    }
}

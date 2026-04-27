package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.query.WarehouseQuery;
import com.berlin.aetherflow.wms.domain.bo.WarehouseBo;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.vo.WarehouseVo;
import com.berlin.aetherflow.wms.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库 Controller。
 * 仓库新增、编辑、列表查询、状态启停
 * @author zhubn
 * @date 2026/4/15
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/wms/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "根据仓库id查询")
    @GetMapping("/{id}")
    public Result<WarehouseVo> get(@PathVariable Long id){
        Warehouse warehouse = warehouseService.getById(id);
        WarehouseVo vo = MapstructUtils.convert(warehouse, WarehouseVo.class);
        return Result.success(vo);
    }

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public Result<PageResult<WarehouseVo>> list(@RequestBody WarehouseQuery query){
        return Result.success(warehouseService.queryList(query));
    }

    @Operation(summary = "根据bo创建仓库")
    @PostMapping
    public Result<List<WarehouseVo>> create(@RequestBody WarehouseBo bo){
        bo.setId(null);
        warehouseService.createWarehouse(bo);
        return Result.success();
    }

    @Operation(summary = "根据bo修改仓库")
    @PutMapping
    public Result<List<WarehouseVo>> update(@RequestBody WarehouseBo bo){
        warehouseService.updateWarehouse(bo);
        return Result.success();
    }

    @Operation(summary = "根据ids删除仓库")
    @DeleteMapping
    public Result<List<WarehouseVo>> delete(@RequestParam List<Long> ids){
        warehouseService.deleteWarehouseByIds(ids);
        return Result.success();
    }
}

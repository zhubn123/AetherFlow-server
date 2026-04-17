package com.berlin.aetherflow.modules.wms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.modules.wms.domain.bo.InventoryBo;
import com.berlin.aetherflow.modules.wms.domain.entity.Inventory;
import com.berlin.aetherflow.modules.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.modules.wms.domain.vo.InventoryVo;
import com.berlin.aetherflow.modules.wms.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "根据仓库id查询")
    @GetMapping("/{id}")
    public Result<InventoryVo> get(@PathVariable Long id){
        Inventory inventory = inventoryService.getById(id);
        InventoryVo vo = MapstructUtils.convert(inventory, InventoryVo.class);
        return Result.success(vo);
    }

    @Operation(summary = "分页查询")
    @PostMapping("/page")
    public Result<List<InventoryVo>> list(@RequestBody InventoryQuery query){
        return Result.success(inventoryService.queryList(query));
    }
}

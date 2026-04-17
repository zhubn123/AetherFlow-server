package com.berlin.aetherflow.modules.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.modules.wms.domain.bo.InventoryBo;
import com.berlin.aetherflow.modules.wms.domain.entity.Inventory;
import com.berlin.aetherflow.modules.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.modules.wms.domain.vo.InventoryVo;
import com.berlin.aetherflow.modules.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.modules.wms.service.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author berlin
* @description 针对表【Inventory(库存表)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
@AllArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
    implements InventoryService {

    private final InventoryMapper inventoryMapper;

    @Override
    public List<InventoryVo> queryList(InventoryQuery query) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(query.getLocationId() != null, Inventory::getLocationId, query.getLocationId())
                .eq(query.getWarehouseId() != null, Inventory::getWarehouseId, query.getWarehouseId())
                .eq(query.getMaterialId() != null, Inventory::getMaterialId, query.getMaterialId())
                .ge(query.getMinQuantity() != null, Inventory::getQuantity, query.getMinQuantity())
                .le(query.getMaxQuantity() != null, Inventory::getQuantity, query.getMaxQuantity());

        IPage<Inventory> page = new Page<>(query.getPageNo(), query.getPageSize());
        page.orders().add(OrderUtil.build(query.getSortBy(), query.getIsAsc()));

        IPage<Inventory> result = inventoryMapper.selectPage(page, lqw);
        return result.getRecords().stream().map(e -> MapstructUtils.convert(e, InventoryVo.class)).toList();
    }
}





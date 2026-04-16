package com.berlin.aetherflow.modules.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.modules.wms.domain.bo.InventoryBo;
import com.berlin.aetherflow.modules.wms.domain.entity.Inventory;
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

    /**
     * 分页查询
     *
     * @param page
     * @param bo
     * @return
     */
    @Override
    public List<InventoryVo> queryList(IPage<Inventory> page, InventoryBo bo) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(bo.getLocationId() != null, Inventory::getLocationId, bo.getLocationId())
                .eq(bo.getWarehouseId() != null, Inventory::getWarehouseId, bo.getWarehouseId())
                .eq(bo.getMaterialId() != null, Inventory::getMaterialId, bo.getMaterialId())
                .ge(bo.getMinQuantity() != null, Inventory::getQuantity, bo.getMinQuantity())
                .le(bo.getMaxQuantity() != null, Inventory::getQuantity, bo.getMaxQuantity());

        IPage<Inventory> result = inventoryMapper.selectPage(page, lqw);
        return result.getRecords().stream().map(e -> MapstructUtils.convert(e, InventoryVo.class)).toList();
    }
}





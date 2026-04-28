package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustmentItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存调整单明细 Mapper。
 */
@Mapper
public interface InventoryAdjustmentItemMapper extends BaseMapper<InventoryAdjustmentItem> {
}

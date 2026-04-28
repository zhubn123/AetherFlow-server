package com.berlin.aetherflow.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.berlin.aetherflow.wms.domain.entity.StockTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水 Mapper。
 */
@Mapper
public interface StockTransactionMapper extends BaseMapper<StockTransaction> {

}

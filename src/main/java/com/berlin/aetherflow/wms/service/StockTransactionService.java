package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.entity.StockTransaction;
import com.berlin.aetherflow.wms.domain.query.StockTransactionQuery;
import com.berlin.aetherflow.wms.domain.vo.StockTransactionVo;

import java.math.BigDecimal;

/**
 * 库存流水 Service。
 */
public interface StockTransactionService extends IService<StockTransaction> {

    PageResult<StockTransactionVo> queryList(StockTransactionQuery query);

    StockTransactionVo getDetailById(Long id);

    void createTransaction(StockChangeBo change, Long areaId, BigDecimal beforeQty, BigDecimal afterQty);
}

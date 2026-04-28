package com.berlin.aetherflow.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentActionBo;
import com.berlin.aetherflow.wms.domain.bo.InventoryAdjustmentBo;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import com.berlin.aetherflow.wms.domain.query.InventoryAdjustmentQuery;
import com.berlin.aetherflow.wms.domain.vo.InventoryAdjustmentDetailVo;
import com.berlin.aetherflow.wms.domain.vo.InventoryAdjustmentVo;

import java.util.List;

/**
 * 库存调整单 Service。
 */
public interface InventoryAdjustmentService extends IService<InventoryAdjustment> {

    PageResult<InventoryAdjustmentVo> queryList(InventoryAdjustmentQuery query);

    InventoryAdjustmentDetailVo getDetailById(Long id);

    Long createInventoryAdjustment(InventoryAdjustmentBo bo);

    Boolean updateInventoryAdjustment(InventoryAdjustmentBo bo);

    Boolean applyAction(Long id, InventoryAdjustmentActionBo bo);

    Boolean removeInventoryAdjustments(List<Long> ids);
}

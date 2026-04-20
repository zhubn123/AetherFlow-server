package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.domain.bo.InboundOrderBo;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.wms.domain.enums.BizCodeTypeConst;
import com.berlin.aetherflow.wms.domain.query.InboundOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.InboundOrderVo;
import com.berlin.aetherflow.wms.mapper.InboundOrderMapper;
import com.berlin.aetherflow.wms.service.InboundOrderItemService;
import com.berlin.aetherflow.wms.service.InboundOrderService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author berlin
 * @description 针对表【inbound_order(入库单)】的数据库操作Service实现
 * @createDate 2026-04-15 16:17:27
 */
@Service
@AllArgsConstructor
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder>
        implements InboundOrderService {

    private final InboundOrderMapper inboundOrderMapper;
    private final InboundOrderItemService inboundOrderItemService;

    @Override
    public List<InboundOrderVo> queryList(InboundOrderQuery query) {
        IPage<InboundOrder> page = new Page<>(query.getPageNo(), query.getPageSize());
        page.orders().add(OrderUtil.build(query.getSortBy(), query.getIsAsc()));

        LambdaQueryWrapper<InboundOrder> lqw = Wrappers.<InboundOrder>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getOrderNo()), InboundOrder::getOrderNo, query.getOrderNo())
                .eq(query.getWarehouseId() != null, InboundOrder::getWarehouseId, query.getWarehouseId())
                .eq(query.getStatus() != null, InboundOrder::getStatus, query.getStatus())
                .ge(query.getInboundStartTime() !=null, InboundOrder::getInboundTime, query.getInboundStartTime())
                .le(query.getInboundEndTime() !=null, InboundOrder::getInboundTime, query.getInboundEndTime())
                .like(StringUtils.isNotBlank(query.getRemark()), InboundOrder::getRemark, query.getRemark());

        IPage<InboundOrder> result = inboundOrderMapper.selectPage(page, lqw);

        return result.getRecords().stream().map(e -> MapstructUtils.convert(e, InboundOrderVo.class)).toList();
    }

    public int createInboundOrder(InboundOrderBo bo) {
        // 生成入库单号
        bo.setOrderNo(CodeGenerate.generateSimple(BizCodeTypeConst.INBOUND_ORDER));
        InboundOrder order = MapstructUtils.convert(bo, InboundOrder.class);

        // 生成入库单详情 TODO InboundOrderBo 里要不要加 InboundOrderItemBo
        // inboundOrderItemService.

        return inboundOrderMapper.insert(order);
    }
}





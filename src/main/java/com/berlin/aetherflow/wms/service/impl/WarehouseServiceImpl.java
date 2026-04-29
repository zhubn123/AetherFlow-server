package com.berlin.aetherflow.wms.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.constant.BizCodeTypeConst;
import com.berlin.aetherflow.wms.domain.query.WarehouseQuery;
import com.berlin.aetherflow.wms.domain.bo.WarehouseBo;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.vo.WarehouseVo;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.WarehouseService;
import com.berlin.aetherflow.wms.support.WmsOptionCacheSupport;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author berlin
 * @description 针对表【warehouse(仓库表)】的数据库操作Service实现
 * @createDate 2026-04-15 16:17:27
 */
@Service
@AllArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse>
        implements WarehouseService {

    private final WarehouseMapper warehouseMapper;
    private final WmsOptionCacheSupport wmsOptionCacheSupport;

    @Override
    public WarehouseVo getByCode(String code) {
        Warehouse warehouse = warehouseMapper.selectByColumn(Warehouse::getWarehouseCode, code);
        return MapstructUtils.convert(warehouse, new WarehouseVo());
    }

    @Override
    public PageResult<WarehouseVo> queryList(WarehouseQuery query) {

        IPage<Warehouse> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<Warehouse> lqw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getWarehouseCode())) {
            lqw.eq(Warehouse::getWarehouseCode, query.getWarehouseCode());
        }
        if (StringUtils.isNotBlank(query.getWarehouseName())) {
            lqw.like(Warehouse::getWarehouseName, query.getWarehouseName());
        }
        IPage<Warehouse> result = warehouseMapper.selectPage(page, lqw);
        List<WarehouseVo> records = result.getRecords().stream()
                .map(e -> MapstructUtils.convert(e, WarehouseVo.class))
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public void createWarehouse(WarehouseBo bo) {
        Warehouse warehouse = MapstructUtils.convert(bo, Warehouse.class);
        // TODO 仓库名称是否重复
        warehouse.setWarehouseCode(CodeGenerate.generateSimple(BizCodeTypeConst.WAREHOUSE));
        warehouseMapper.insert(warehouse);
        wmsOptionCacheSupport.evictWarehouseRelatedOptions();
    }

    @Override
    public void updateWarehouse(WarehouseBo bo) {
        Warehouse warehouse = MapstructUtils.convert(bo, Warehouse.class);
        Assert.isFalse(Objects.isNull(warehouse), "请填写仓库信息");
        // 更新时不允许通过 BO 覆盖系统生成的编码。
        warehouse.setWarehouseCode(null);
        warehouseMapper.updateById(warehouse);
        wmsOptionCacheSupport.evictWarehouseRelatedOptions();
    }

    @Override
    public void deleteWarehouseByIds(List<Long> ids) {
        warehouseMapper.deleteByIds(ids);
        if (ids != null && !ids.isEmpty()) {
            wmsOptionCacheSupport.evictWarehouseRelatedOptions();
        }
    }
}





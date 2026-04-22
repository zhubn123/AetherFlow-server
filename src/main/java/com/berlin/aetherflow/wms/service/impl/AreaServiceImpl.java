package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.constant.BizCodeTypeConst;
import com.berlin.aetherflow.wms.domain.bo.AreaBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.AreaQuery;
import com.berlin.aetherflow.wms.domain.vo.AreaVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.AreaService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 区域 Service 实现。
 */
@Service
@AllArgsConstructor
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {

    private final AreaMapper areaMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public PageResult<AreaVo> queryList(AreaQuery query) {
        IPage<Area> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<Area> lqw = Wrappers.<Area>lambdaQuery()
                .eq(query.getWarehouseId() != null, Area::getWarehouseId, query.getWarehouseId())
                .eq(StringUtils.isNotBlank(query.getAreaCode()), Area::getAreaCode, query.getAreaCode())
                .like(StringUtils.isNotBlank(query.getAreaName()), Area::getAreaName, query.getAreaName())
                .eq(StringUtils.isNotBlank(query.getAreaType()), Area::getAreaType, query.getAreaType())
                .eq(query.getStatus() != null, Area::getStatus, query.getStatus())
                .like(StringUtils.isNotBlank(query.getRemark()), Area::getRemark, query.getRemark());

        IPage<Area> result = areaMapper.selectPage(page, lqw);
        List<AreaVo> records = result.getRecords().stream()
                .map(e -> MapstructUtils.convert(e, AreaVo.class))
                .toList();
        fillWarehouseDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public Long createArea(AreaBo bo) {
        validateWarehouseExists(bo.getWarehouseId());
        Area area = MapstructUtils.convert(bo, Area.class);
        if (area.getStatus() == null) {
            area.setStatus(0);
        }
        area.setAreaCode(CodeGenerate.generateSimple(BizCodeTypeConst.AREA));
        areaMapper.insert(area);
        return area.getId();
    }

    @Override
    public Boolean updateArea(AreaBo bo) {
        Area exists = getById(bo.getId());
        if (exists == null) {
            throw new RuntimeException("区域不存在");
        }
        validateWarehouseExists(bo.getWarehouseId());
        Area area = MapstructUtils.convert(bo, Area.class);
        area.setAreaCode(null);
        return updateById(area);
    }

    @Override
    public Boolean removeAreas(List<Long> ids) {
        return removeByIds(ids);
    }

    private void validateWarehouseExists(Long warehouseId) {
        if (warehouseId == null) {
            throw new RuntimeException("所属仓库不能为空");
        }
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new RuntimeException("所属仓库不存在");
        }
    }

    /**
     * 填充区域列表的仓库展示字段（编码、名称）。
     *
     * @param records 区域列表
     */
    private void fillWarehouseDisplay(List<AreaVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(AreaVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (warehouseIds.isEmpty()) {
            return;
        }

        Map<Long, Warehouse> warehouseMap = warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        for (AreaVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse == null) {
                continue;
            }
            record.setWarehouseCode(warehouse.getWarehouseCode());
            record.setWarehouseName(warehouse.getWarehouseName());
        }
    }
}

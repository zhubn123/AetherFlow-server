package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.WmsOptionQuery;
import com.berlin.aetherflow.wms.domain.vo.WmsOptionVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.WmsOptionService;
import com.berlin.aetherflow.wms.support.WmsOptionCacheSupport;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * WMS 主数据选项服务实现。
 */
@Service
@AllArgsConstructor
public class WmsOptionServiceImpl implements WmsOptionService {

    private static final int LOCATION_OPTION_LIMIT = 200;
    private static final int MATERIAL_OPTION_LIMIT = 100;

    private final WarehouseMapper warehouseMapper;
    private final AreaMapper areaMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;
    private final WmsOptionCacheSupport wmsOptionCacheSupport;

    @Override
    public List<WmsOptionVo> queryWarehouseOptions(WmsOptionQuery query) {
        WmsOptionQuery normalized = normalizeQuery(query);
        // 先归一化查询条件，再拼缓存 key，避免 null/空格/大小写导致重复缓存。
        return wmsOptionCacheSupport.getOrLoad(
                WmsOptionCacheSupport.NAMESPACE_WAREHOUSE,
                buildCacheKey(normalized),
                () -> loadWarehouseOptions(normalized)
        );
    }

    @Override
    public List<WmsOptionVo> queryAreaOptions(WmsOptionQuery query) {
        WmsOptionQuery normalized = normalizeQuery(query);
        return wmsOptionCacheSupport.getOrLoad(
                WmsOptionCacheSupport.NAMESPACE_AREA,
                buildCacheKey(normalized),
                () -> loadAreaOptions(normalized)
        );
    }

    @Override
    public List<WmsOptionVo> queryLocationOptions(WmsOptionQuery query) {
        WmsOptionQuery normalized = normalizeQuery(query);
        // A003 阶段不允许无仓库全量拉库位，避免把大表一次性灌进 JVM 和 Redis。
        if (normalized.getWarehouseId() == null) {
            throw new IllegalArgumentException("查询库位选项时必须传 warehouseId");
        }
        return wmsOptionCacheSupport.getOrLoad(
                WmsOptionCacheSupport.NAMESPACE_LOCATION,
                buildCacheKey(normalized),
                () -> loadLocationOptions(normalized)
        );
    }

    @Override
    public List<WmsOptionVo> queryMaterialOptions(WmsOptionQuery query) {
        WmsOptionQuery normalized = normalizeQuery(query);
        // A003 阶段不允许无关键词全量拉物料，后续大数据量检索由 A005 接管。
        if (StringUtils.isBlank(normalized.getKeyword())) {
            throw new IllegalArgumentException("查询物料选项时必须传 keyword");
        }
        return wmsOptionCacheSupport.getOrLoad(
                WmsOptionCacheSupport.NAMESPACE_MATERIAL,
                buildCacheKey(normalized),
                () -> loadMaterialOptions(normalized)
        );
    }

    private List<WmsOptionVo> loadWarehouseOptions(WmsOptionQuery query) {
        LambdaQueryWrapper<Warehouse> lqw = Wrappers.<Warehouse>lambdaQuery()
                .eq(Warehouse::getStatus, resolveStatus(query))
                .orderByAsc(Warehouse::getWarehouseCode);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            lqw.and(wrapper -> wrapper.like(Warehouse::getWarehouseCode, query.getKeyword())
                    .or()
                    .like(Warehouse::getWarehouseName, query.getKeyword()));
        }
        return warehouseMapper.selectList(lqw).stream()
                .map(this::toWarehouseOption)
                .toList();
    }

    private List<WmsOptionVo> loadAreaOptions(WmsOptionQuery query) {
        LambdaQueryWrapper<Area> lqw = Wrappers.<Area>lambdaQuery()
                .eq(Area::getStatus, resolveStatus(query))
                // 区域选项允许按仓库联动过滤，避免跨仓误选。
                .eq(query.getWarehouseId() != null, Area::getWarehouseId, query.getWarehouseId())
                .orderByAsc(Area::getAreaCode);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            lqw.and(wrapper -> wrapper.like(Area::getAreaCode, query.getKeyword())
                    .or()
                    .like(Area::getAreaName, query.getKeyword()));
        }
        return areaMapper.selectList(lqw).stream()
                .map(this::toAreaOption)
                .toList();
    }

    private List<WmsOptionVo> loadLocationOptions(WmsOptionQuery query) {
        LambdaQueryWrapper<Location> lqw = Wrappers.<Location>lambdaQuery()
                .eq(Location::getStatus, resolveStatus(query))
                // 库位选项同时支持仓库、区域两级联动过滤。
                .eq(query.getWarehouseId() != null, Location::getWarehouseId, query.getWarehouseId())
                .eq(query.getAreaId() != null, Location::getAreaId, query.getAreaId())
                .orderByAsc(Location::getLocationCode)
                // A003 先做轻量选项，超过上限的大数据量滚动加载放到 A005。
                .last("limit " + LOCATION_OPTION_LIMIT);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            lqw.and(wrapper -> wrapper.like(Location::getLocationCode, query.getKeyword())
                    .or()
                    .like(Location::getLocationName, query.getKeyword()));
        }
        return locationMapper.selectList(lqw).stream()
                .map(this::toLocationOption)
                .toList();
    }

    private List<WmsOptionVo> loadMaterialOptions(WmsOptionQuery query) {
        LambdaQueryWrapper<Material> lqw = Wrappers.<Material>lambdaQuery()
                .eq(Material::getStatus, resolveStatus(query))
                .orderByAsc(Material::getMaterialCode)
                // 物料规模最容易先涨起来，A003 先做关键词检索 + 截断保护。
                .last("limit " + MATERIAL_OPTION_LIMIT);
        if (StringUtils.isNotBlank(query.getKeyword())) {
            lqw.and(wrapper -> wrapper.like(Material::getMaterialCode, query.getKeyword())
                    .or()
                    .like(Material::getMaterialName, query.getKeyword()));
        }
        return materialMapper.selectList(lqw).stream()
                .map(this::toMaterialOption)
                .toList();
    }

    private WmsOptionVo toWarehouseOption(Warehouse warehouse) {
        WmsOptionVo optionVo = new WmsOptionVo();
        optionVo.setId(warehouse.getId());
        optionVo.setCode(warehouse.getWarehouseCode());
        optionVo.setName(warehouse.getWarehouseName());
        optionVo.setStatus(warehouse.getStatus());
        return optionVo;
    }

    private WmsOptionVo toAreaOption(Area area) {
        WmsOptionVo optionVo = new WmsOptionVo();
        optionVo.setId(area.getId());
        optionVo.setCode(area.getAreaCode());
        optionVo.setName(area.getAreaName());
        optionVo.setStatus(area.getStatus());
        return optionVo;
    }

    private WmsOptionVo toLocationOption(Location location) {
        WmsOptionVo optionVo = new WmsOptionVo();
        optionVo.setId(location.getId());
        optionVo.setCode(location.getLocationCode());
        optionVo.setName(location.getLocationName());
        optionVo.setStatus(location.getStatus());
        return optionVo;
    }

    private WmsOptionVo toMaterialOption(Material material) {
        WmsOptionVo optionVo = new WmsOptionVo();
        optionVo.setId(material.getId());
        optionVo.setCode(material.getMaterialCode());
        optionVo.setName(material.getMaterialName());
        optionVo.setStatus(material.getStatus());
        return optionVo;
    }

    private WmsOptionQuery normalizeQuery(WmsOptionQuery query) {
        WmsOptionQuery normalized = new WmsOptionQuery();
        if (query == null) {
            // 选项接口默认只查启用数据。
            normalized.setStatus(0);
            return normalized;
        }
        normalized.setStatus(query.getStatus() != null ? query.getStatus() : 0);
        normalized.setWarehouseId(query.getWarehouseId());
        normalized.setAreaId(query.getAreaId());
        normalized.setKeyword(normalizeKeyword(query.getKeyword()));
        return normalized;
    }

    private Integer resolveStatus(WmsOptionQuery query) {
        return query == null || query.getStatus() == null ? 0 : query.getStatus();
    }

    private String buildCacheKey(WmsOptionQuery query) {
        Integer status = resolveStatus(query);

        StringBuilder key = new StringBuilder();
        key.append("status:").append(status);

        if (query == null) {
            return key.toString();
        }

        if (query.getWarehouseId() != null) {
            key.append(":warehouse:").append(query.getWarehouseId());
        }

        if (query.getAreaId() != null) {
            key.append(":area:").append(query.getAreaId());
        }

        // keyword 统一 trim + lower-case 后再入 key，避免 "abc" 和 " ABC " 产生两份缓存。
        String keyword = normalizeKeyword(query.getKeyword());
        if (StringUtils.isNotBlank(keyword)) {
            key.append(":kw:").append(keyword);
        }

        return key.toString();
    }

    private String normalizeKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return null;
        }
        return keyword.trim().toLowerCase(Locale.ROOT);
    }
}

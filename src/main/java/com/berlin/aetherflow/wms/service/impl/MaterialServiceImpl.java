package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.constant.BizCodeTypeConst;
import com.berlin.aetherflow.wms.domain.bo.MaterialBo;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.query.MaterialQuery;
import com.berlin.aetherflow.wms.domain.vo.MaterialVo;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.service.MaterialService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
* @author berlin
* @description 针对表【material(物料表)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
@AllArgsConstructor
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material>
        implements MaterialService {

    private final MaterialMapper materialMapper;

    @Override
    public PageResult<MaterialVo> queryList(MaterialQuery query) {
        IPage<Material> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<Material> lqw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getMaterialCode())) {
            lqw.eq(Material::getMaterialCode, query.getMaterialCode());
        }
        if (StringUtils.isNotBlank(query.getMaterialName())) {
            lqw.like(Material::getMaterialName, query.getMaterialName());
        }
        if (StringUtils.isNotBlank(query.getSpecification())) {
            lqw.like(Material::getSpecification, query.getSpecification());
        }
        if (StringUtils.isNotBlank(query.getUnit())) {
            lqw.eq(Material::getUnit, query.getUnit());
        }
        if (query.getStatus() != null) {
            lqw.eq(Material::getStatus, query.getStatus());
        }
        if (StringUtils.isNotBlank(query.getRemark())) {
            lqw.like(Material::getRemark, query.getRemark());
        }

        IPage<Material> result = materialMapper.selectPage(page, lqw);
        List<MaterialVo> records = result.getRecords().stream()
                .map(e -> MapstructUtils.convert(e, MaterialVo.class))
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public Long createMaterial(MaterialBo bo) {
        Material material = MapstructUtils.convert(bo, Material.class);
        if (material.getStatus() == null) {
            material.setStatus(0);
        }
        material.setMaterialCode(CodeGenerate.generateSimple(BizCodeTypeConst.MATERIAL));
        materialMapper.insert(material);
        return material.getId();
    }

    @Override
    public Boolean updateMaterial(MaterialBo bo) {
        Material exists = getById(bo.getId());
        if (Objects.isNull(exists)) {
            throw new RuntimeException("物料不存在");
        }
        Material material = MapstructUtils.convert(bo, Material.class);
        material.setMaterialCode(null);
        return updateById(material);
    }

    @Override
    public Boolean removeMaterials(List<Long> ids) {
        return removeByIds(ids);
    }

}





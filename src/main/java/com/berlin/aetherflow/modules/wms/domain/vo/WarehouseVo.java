package com.berlin.aetherflow.modules.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.modules.wms.domain.entity.Warehouse;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WarehouseVo
 * 仓库信息
 * @author zhubn
 * @date 2026/4/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AutoMapper(target = Warehouse.class,convertGenerate = false)
public class WarehouseVo extends BaseEntity {

    private Long id;

    /**
     * 仓库编码。
     */
    private String warehouseCode;

    /**
     * 仓库名称。
     */
    private String warehouseName;

    /**
     * 仓库状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}


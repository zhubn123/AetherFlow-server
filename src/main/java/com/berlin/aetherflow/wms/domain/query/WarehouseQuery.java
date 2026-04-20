package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WarehouseQuery
 *
 * @author zhubn
 * @date 2026/4/16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AutoMapper(target = Warehouse.class, reverseConvertGenerate = false)
public class WarehouseQuery extends PageQuery {

    /**
     * id
     */
    @NotNull(message = "不能为空")
    private Long id;

    /**
     * 编号
     */
    private String warehouseCode;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    private String warehouseName;

    /**
     * 仓库状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

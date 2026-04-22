package com.berlin.aetherflow.wms.domain.bo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.Area;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 区域业务对象。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Area.class, reverseConvertGenerate = false)
public class AreaBo extends BaseEntity {

    private Long id;

    /**
     * 所属仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域编码。
     */
    private String areaCode;

    /**
     * 区域名称。
     */
    private String areaName;

    /**
     * 区域类型。
     */
    private String areaType;

    /**
     * 状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}

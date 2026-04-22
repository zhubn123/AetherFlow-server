package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.Area;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 区域查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Area.class, reverseConvertGenerate = false)
public class AreaQuery extends PageQuery {

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

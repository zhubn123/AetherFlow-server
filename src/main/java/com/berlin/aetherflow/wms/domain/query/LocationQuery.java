package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.common.PageQuery;
import com.berlin.aetherflow.wms.domain.entity.Location;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 库位实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Location.class, reverseConvertGenerate = false)
public class LocationQuery extends PageQuery {

    private Long id;

    /**
     * 所属仓库ID。
     */
    private Long warehouseId;

    /**
     * 所属区域ID。
     */
    private Long areaId;

    /**
     * 库位编码。
     */
    private String locationCode;

    /**
     * 库位名称。
     */
    private String locationName;

    /**
     * 库位状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}

package com.berlin.aetherflow.wms.domain.query;

import lombok.Data;

/**
 * WMS 主数据选项查询参数。
 */
@Data
public class WmsOptionQuery {

    /**
     * 主数据状态（默认 0=启用）。
     */
    private Integer status;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 关键字（编码/名称模糊搜索）。
     */
    private String keyword;
}

package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * WMS 轻量选项返回对象。
 */
@Data
public class WmsOptionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private String name;

    private Integer status;
}

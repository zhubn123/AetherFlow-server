package com.berlin.aetherflow.common;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * PageQuery
 *
 * @author zhubn
 * @date 2026/4/15
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码 (默认 1)
     */
    @Schema(defaultValue = "1",description = "页码 (默认 1)")
    private Integer pageNo = 1;

    /**
     * 每页条数 (默认 10)
     */
    @Schema(defaultValue = "10",description = "每页条数 (默认 10)")
    private Integer pageSize = 10;

    /**
     * 排序字段 (前端传参，例如 "create_time")
     */
    private String sortBy;

    /**
     * 是否升序 (前端传参，true=升序，false=降序)
     */
    private Boolean isAsc;
}

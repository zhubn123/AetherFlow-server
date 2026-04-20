package com.berlin.aetherflow.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用分页返回结构。
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码。
     */
    private Long pageNo;

    /**
     * 每页条数。
     */
    private Long pageSize;

    /**
     * 总记录数。
     */
    private Long total;

    /**
     * 总页数。
     */
    private Long pages;

    /**
     * 当前页数据。
     */
    private List<T> records;

    public static <T> PageResult<T> of(Long pageNo, Long pageSize, Long total, Long pages, List<T> records) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setPageNo(pageNo);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(total);
        pageResult.setPages(pages);
        pageResult.setRecords(records);
        return pageResult;
    }
}

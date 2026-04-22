package com.berlin.aetherflow.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

/**
 * OrderUtil
 *
 * @author zhubn
 * @date 2026/4/16
 */
public class OrderUtil {

    public static OrderItem build(String column, Boolean isAsc) {
        if (column == null || isAsc == null) {
            return null;
        }
        return Boolean.TRUE.equals(isAsc)
                ? OrderItem.asc(column)
                : OrderItem.desc(column);
    }

    /**
     * 向分页对象安全添加排序项：当排序字段或方向为空时不添加，避免写入 null 触发分页插件异常。
     *
     * @param page   分页对象
     * @param column 排序字段
     * @param isAsc  排序方向（true=升序，false=降序）
     */
    public static void addOrder(IPage<?> page, String column, Boolean isAsc) {
        if (page == null) {
            return;
        }
        OrderItem orderItem = build(column, isAsc);
        if (orderItem != null) {
            page.orders().add(orderItem);
        }
    }
}

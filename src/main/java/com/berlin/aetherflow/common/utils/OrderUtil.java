package com.berlin.aetherflow.common.utils;

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
}
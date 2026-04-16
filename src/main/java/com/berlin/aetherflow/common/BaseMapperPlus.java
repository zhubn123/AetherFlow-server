package com.berlin.aetherflow.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.List;

/**
 * BaseMapperPlus
 *
 * @author zhubn
 * @date 2026/4/15
 */

public interface BaseMapperPlus<T> extends BaseMapper<T> {

    /**
     * 根据唯一编号查询单条数据
     * <P>注意 ⚠：多条报错</P>
     *
     * @param column
     * @param v
     * @return
     */
    default <V> T selectByColumn(SFunction<T, V> column, V v) {
        LambdaQueryWrapper<T> lqw = new LambdaQueryWrapper<>();
        lqw.eq(column,v);
        return this.selectOne(lqw);
    }

    /**
     * 根据两个列查询
     */
    default <A,B> List<T> selectByColumn(SFunction<T, A> column1, A v1, SFunction<T, B> column2, B v2) {
        LambdaQueryWrapper<T> lqw = new LambdaQueryWrapper<>();
        lqw.eq(column1,v1);
        lqw.eq(column2,v2);
        return this.selectList(lqw);
    }

    /**
     * 根据单列查列表
     *
     * @param column
     * @param v
     * @return
     */
    default <V> List<T> selectListByColumn(SFunction<T, V> column, V v) {
        LambdaQueryWrapper<T> lqw = new LambdaQueryWrapper<>();
        lqw.eq(column,v);
        return this.selectList(lqw);
    }
}

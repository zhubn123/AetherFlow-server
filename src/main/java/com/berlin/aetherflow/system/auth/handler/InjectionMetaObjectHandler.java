package com.berlin.aetherflow.system.auth.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充处理器
 *
 * @author zhubn
 * @date 2026/4/16
 */
@Slf4j
@Component  // 添加此注解，让Spring管理这个处理器
public class InjectionMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        try {
            // 填充BaseEntity相关字段
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 如果createTime为空，则填充当前时间
                if (ObjectUtil.isNull(baseEntity.getCreateTime())) {
                    baseEntity.setCreateTime(LocalDateTime.now());
                    // TODO 为当前登录用户
                    baseEntity.setCreateBy("temp");
                }
                // 设置updateTime为当前时间（插入时和更新时都设置）
                baseEntity.setUpdateTime(LocalDateTime.now());
                // TODO update_by 为当前登录用户
                baseEntity.setUpdateBy("temp");
            }

        } catch (Exception e) {
            log.error("自动注入异常 => ", e);
            throw new RuntimeException("自动注入异常 => " + e.getMessage(), e);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        try {
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
                // 更新时间填充(总是设置为当前时间)
                baseEntity.setUpdateTime(LocalDateTime.now());
                // TODO update_by 为当前登录用户
                baseEntity.setUpdateBy("temp");
            }
        } catch (Exception e) {
            log.error("自动注入异常 => ", e);
            throw new RuntimeException("自动注入异常 => " + e.getMessage(), e);
        }
    }
}
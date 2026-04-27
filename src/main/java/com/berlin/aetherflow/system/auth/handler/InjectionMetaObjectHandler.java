package com.berlin.aetherflow.system.auth.handler;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
                String operator = resolveOperator();
                // 如果createTime为空，则填充当前时间
                if (ObjectUtil.isNull(baseEntity.getCreateTime())) {
                    baseEntity.setCreateTime(LocalDateTime.now());
                }
                if (StringUtils.isBlank(baseEntity.getCreateBy())) {
                    baseEntity.setCreateBy(operator);
                }
                // 设置updateTime为当前时间（插入时和更新时都设置）
                baseEntity.setUpdateTime(LocalDateTime.now());
                if (StringUtils.isBlank(baseEntity.getUpdateBy())) {
                    baseEntity.setUpdateBy(operator);
                }
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
                baseEntity.setUpdateBy(resolveOperator());
            }
        } catch (Exception e) {
            log.error("自动注入异常 => ", e);
            throw new RuntimeException("自动注入异常 => " + e.getMessage(), e);
        }
    }

    /**
     * 解析当前操作人：优先登录会话中的操作人名称，其次登录ID，获取不到时回退为 system。
     */
    private String resolveOperator() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                Object operatorName = StpUtil.getTokenSession().get("operatorName");
                if (operatorName != null && StringUtils.isNotBlank(String.valueOf(operatorName))) {
                    return String.valueOf(operatorName);
                }
                return String.valueOf(loginId);
            }
        } catch (Exception ex) {
            log.debug("当前上下文无登录信息，审计字段回退为 system", ex);
        }
        return "system";
    }
}

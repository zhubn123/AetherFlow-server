package com.berlin.aetherflow.system.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 安全审计日志实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class SysAuditLog extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 ID（匿名事件可为空）
     */
    private Long userId;

    /**
     * 用户名（匿名事件可为空）
     */
    private String username;

    /**
     * 事件类型（LOGIN/PROFILE/PASSWORD）
     */
    private String eventType;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 请求路径
     */
    private String requestUri;

    /**
     * 客户端 IP
     */
    private String clientIp;

    /**
     * 执行结果（1成功 0失败）
     */
    private Integer result;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 事件时间
     */
    private LocalDateTime occurTime;
}

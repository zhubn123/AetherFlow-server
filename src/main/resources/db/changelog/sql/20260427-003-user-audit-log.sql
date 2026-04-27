--liquibase formatted sql

--changeset berlin:20260427-003-user-audit-log labels:user-domain context:all
--comment: 用户域安全审计日志表（A006-3）
create table if not exists sys_audit_log
(
    id          bigint primary key comment '主键ID',
    user_id     bigint       null comment '用户ID（匿名事件可空）',
    username    varchar(64)  null comment '用户名（匿名事件可空）',
    event_type  varchar(32)  not null comment '事件类型（LOGIN/PROFILE/PASSWORD）',
    event_name  varchar(64)  not null comment '事件名称',
    request_uri varchar(255) null comment '请求路径',
    client_ip   varchar(64)  null comment '客户端IP',
    result      tinyint      not null default 0 comment '执行结果（1成功 0失败）',
    message     varchar(255) null comment '结果消息',
    occur_time  datetime     not null default CURRENT_TIMESTAMP comment '事件发生时间',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_sys_audit_log_user_id (user_id),
    key idx_sys_audit_log_event_type (event_type),
    key idx_sys_audit_log_occur_time (occur_time)
) engine = InnoDB comment '系统安全审计日志表'
  collate = utf8mb4_unicode_ci;

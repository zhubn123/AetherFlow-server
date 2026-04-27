--liquibase formatted sql

--changeset berlin:20260427-001-user-domain-base labels:user-domain context:all
--comment: 用户域基础表（sys_user/sys_role/sys_user_role）
create table if not exists sys_user
(
    id               bigint primary key comment '主键ID',
    username         varchar(64)  not null comment '登录用户名',
    password_hash    varchar(255) not null comment '密码哈希',
    nickname         varchar(64)  not null default '' comment '用户昵称',
    email            varchar(128) null comment '邮箱',
    phone            varchar(32)  null comment '手机号',
    status           tinyint      not null default 0 comment '状态（0正常 1停用 2锁定）',
    login_fail_count int          not null default 0 comment '连续登录失败次数',
    lock_until       datetime     null comment '锁定截止时间',
    last_login_time  datetime     null comment '最后登录时间',
    create_by        varchar(64)  not null default '' comment '创建人',
    create_time      datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by        varchar(64)  not null default '' comment '更新人',
    update_time      datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_user_username (username),
    unique key uk_sys_user_email (email)
) engine = InnoDB comment '系统用户表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_role
(
    id          bigint primary key comment '主键ID',
    role_key    varchar(64)  not null comment '角色标识（如 admin/operator/viewer）',
    role_name   varchar(64)  not null comment '角色名称',
    status      tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark      varchar(255) not null default '' comment '备注',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_role_key (role_key)
) engine = InnoDB comment '系统角色表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_user_role
(
    id          bigint primary key comment '主键ID',
    user_id     bigint      not null comment '用户ID',
    role_id     bigint      not null comment '角色ID',
    create_by   varchar(64) not null default '' comment '创建人',
    create_time datetime    not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64) not null default '' comment '更新人',
    update_time datetime    not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_user_role (user_id, role_id),
    key idx_sys_user_role_user_id (user_id),
    key idx_sys_user_role_role_id (role_id)
) engine = InnoDB comment '用户角色关联表'
  collate = utf8mb4_unicode_ci;

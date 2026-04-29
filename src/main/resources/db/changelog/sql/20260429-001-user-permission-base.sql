--liquibase formatted sql

--changeset berlin:20260429-001-user-permission-base labels:user-domain context:all
--comment: 用户域权限模型基础表（sys_permission/sys_role_permission）
create table if not exists sys_permission
(
    id          bigint primary key comment '主键ID',
    perm_key    varchar(128) not null comment '权限标识（如 wms:warehouse:view）',
    perm_name   varchar(128) not null comment '权限名称',
    module      varchar(64)  not null comment '所属模块',
    action      varchar(64)  not null comment '动作标识（view/manage）',
    status      tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark      varchar(255) not null default '' comment '备注',
    create_by   varchar(64)  not null default '' comment '创建人',
    create_time datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)  not null default '' comment '更新人',
    update_time datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_permission_perm_key (perm_key),
    key idx_sys_permission_module (module)
) engine = InnoDB comment '系统权限表'
  collate = utf8mb4_unicode_ci;

create table if not exists sys_role_permission
(
    id            bigint primary key comment '主键ID',
    role_id       bigint      not null comment '角色ID',
    permission_id bigint      not null comment '权限ID',
    create_by     varchar(64) not null default '' comment '创建人',
    create_time   datetime    not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64) not null default '' comment '更新人',
    update_time   datetime    not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_sys_role_permission (role_id, permission_id),
    key idx_sys_role_permission_role_id (role_id),
    key idx_sys_role_permission_permission_id (permission_id)
) engine = InnoDB comment '角色权限关联表'
  collate = utf8mb4_unicode_ci;

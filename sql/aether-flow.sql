create database if not exists aether_flow;

use aether_flow;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    Account  varchar(256)                           not null comment '账号',
    Password varchar(512)                           not null comment '密码',
    Name     varchar(256)                           null comment '用户昵称',
    Avatar   varchar(1024)                          null comment '用户头像',
    Profile  varchar(512)                           null comment '用户简介',
    Role     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    create_by    varchar(64)                            NULL DEFAULT '' COMMENT '创建人',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by    varchar(64)                            NULL DEFAULT '' COMMENT '更新人',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    status       tinyint      default 0                 not null comment '帐号状态（0正常 1停用）'
) comment '用户' collate = utf8mb4_unicode_ci;

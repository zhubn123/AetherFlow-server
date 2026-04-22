create database if not exists aether_flow;

use aether_flow;

-- 仓库表
create table if not exists warehouse
(
    id             bigint primary key comment '主键ID',
    warehouse_code varchar(32)  not null comment '仓库编码',
    warehouse_name varchar(64)  not null comment '仓库名称',
    status         tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark         varchar(255) not null default '' comment '备注',
    create_by      varchar(64)  not null default '' comment '创建人',
    create_time    datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by      varchar(64)  not null default '' comment '更新人',
    update_time    datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_warehouse_code (warehouse_code)
) engine = InnoDB comment '仓库表'
  collate = utf8mb4_unicode_ci;

-- 库位表
create table if not exists location
(
    id            bigint primary key comment '主键ID',
    warehouse_id  bigint       not null comment '所属仓库ID',
    location_code varchar(32)  not null comment '库位编码',
    location_name varchar(64)  not null comment '库位名称',
    status        tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark        varchar(255) not null default '' comment '备注',
    create_by     varchar(64)  not null default '' comment '创建人',
    create_time   datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)  not null default '' comment '更新人',
    update_time   datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_location_code (location_code),
    key idx_location_warehouse_id (warehouse_id)
) engine = InnoDB comment '库位表'
  collate = utf8mb4_unicode_ci;

-- 物料表
create table if not exists material
(
    id            bigint primary key comment '主键ID',
    material_code varchar(32)  not null comment '物料编码',
    material_name varchar(128) not null comment '物料名称',
    specification varchar(128) null comment '规格型号',
    unit          varchar(32)  null comment '计量单位',
    status        tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark        varchar(255) not null default '' comment '备注',
    create_by     varchar(64)  not null default '' comment '创建人',
    create_time   datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)  not null default '' comment '更新人',
    update_time   datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_material_code (material_code)
) engine = InnoDB comment '物料表'
  collate = utf8mb4_unicode_ci;

-- 入库单
create table if not exists inbound_order
(
    id           bigint primary key comment '主键ID',
    order_no     varchar(32)    not null comment '入库单号',
    warehouse_id bigint         not null comment '仓库ID',
    location_id  bigint         null comment '库位ID',
    status       tinyint        not null default 0 comment '状态（0草稿 1已确认）',
    total_qty    decimal(18, 2) not null default 0 comment '总数量',
    inbound_time datetime       not null default CURRENT_TIMESTAMP comment '入库时间',
    remark       varchar(255)   not null default '' comment '备注',
    create_by    varchar(64)    not null default '' comment '创建人',
    create_time  datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by    varchar(64)    not null default '' comment '更新人',
    update_time  datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_inbound_order_no (order_no),
    key idx_inbound_warehouse_id (warehouse_id),
    key idx_inbound_location_id (location_id)
) engine = InnoDB comment '入库单'
  collate = utf8mb4_unicode_ci;

-- 入库单明细
create table if not exists inbound_order_item
(
    id          bigint primary key comment '主键ID',
    order_id    bigint         not null comment '入库单ID',
    material_id bigint         not null comment '物料ID',
    qty         decimal(18, 2) not null default 0 comment '入库数量',
    remark      varchar(255)   not null default '' comment '备注',
    create_by   varchar(64)    not null default '' comment '创建人',
    create_time datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)    not null default '' comment '更新人',
    update_time datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_inbound_item_order_id (order_id),
    key idx_inbound_item_material_id (material_id)
) engine = InnoDB comment '入库单明细'
  collate = utf8mb4_unicode_ci;

-- 出库单
create table if not exists outbound_order
(
    id            bigint primary key comment '主键ID',
    order_no      varchar(32)    not null comment '出库单号',
    warehouse_id  bigint         not null comment '仓库ID',
    location_id   bigint         null comment '库位ID',
    status        tinyint        not null default 0 comment '状态（0草稿 1已确认）',
    total_qty     decimal(18, 2) not null default 0 comment '总数量',
    outbound_time datetime       null comment '出库时间',
    remark        varchar(255)   not null default '' comment '备注',
    create_by     varchar(64)    not null default '' comment '创建人',
    create_time   datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)    not null default '' comment '更新人',
    update_time   datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_outbound_order_no (order_no),
    key idx_outbound_warehouse_id (warehouse_id),
    key idx_outbound_location_id (location_id)
) engine = InnoDB comment '出库单'
  collate = utf8mb4_unicode_ci;

-- 出库单明细
create table if not exists outbound_order_item
(
    id          bigint primary key comment '主键ID',
    order_id    bigint         not null comment '出库单ID',
    material_id bigint         not null comment '物料ID',
    qty         decimal(18, 2) not null default 0 comment '出库数量',
    remark      varchar(255)   not null default '' comment '备注',
    create_by   varchar(64)    not null default '' comment '创建人',
    create_time datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)    not null default '' comment '更新人',
    update_time datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_outbound_item_order_id (order_id),
    key idx_outbound_item_material_id (material_id)
) engine = InnoDB comment '出库单明细'
  collate = utf8mb4_unicode_ci;

-- 库存表
create table if not exists inventory
(
    id              bigint primary key comment '主键ID',
    warehouse_id    bigint         not null comment '仓库ID',
    location_id     bigint         not null comment '库位ID',
    material_id     bigint         not null comment '物料ID',
    quantity        decimal(18, 2) not null default 0 comment '当前库存',
    locked_quantity decimal(18, 2) not null default 0 comment '锁定库存',
    create_by       varchar(64)    not null default '' comment '创建人',
    create_time     datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by       varchar(64)    not null default '' comment '更新人',
    update_time     datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_stock_dim (warehouse_id, location_id, material_id),
    key idx_stock_material_id (material_id)
) engine = InnoDB comment '库存表'
  collate = utf8mb4_unicode_ci;

-- =============================================
-- 变更记录（2026-04-17）：入库单模型优化
-- 变更原因：
-- 1) inbound_order 的 location_id / total_qty 放在表头会限制一单多库位，并造成与明细数量不一致风险
-- 2) inbound_time 在草稿阶段不应强制写入，改为可空更符合业务语义
-- 3) inbound_order_item 需要行号、目标库位、计划数量与实收数量，支撑部分收货与差异处理
-- =============================================

alter table inbound_order
    drop index idx_inbound_location_id,
    drop column location_id,
    drop column total_qty,
    modify column inbound_time datetime null comment '实际入库时间';

alter table inbound_order_item
    change column qty planned_qty decimal(18, 2) not null default 0 comment '计划入库数量',
    add column line_no      int            not null default 1 comment '行号' after order_id,
    add column location_id  bigint         null comment '目标库位ID' after material_id,
    add column received_qty decimal(18, 2) not null default 0 comment '已入库数量' after planned_qty,
    add unique key uk_inbound_item_line (order_id, line_no),
    add key idx_inbound_item_location_id (location_id);

-- =============================================
-- 变更记录（2026-04-17）：出库单模型优化
-- 变更原因：
-- 1) outbound_order 的 location_id / total_qty 放在表头会限制一单多库位，并造成与明细数量不一致风险
-- 2) outbound_order_item 需要行号、来源库位、计划数量与实发数量，支撑部分拣货与差异处理
-- =============================================

alter table outbound_order
    drop index idx_outbound_location_id,
    drop column location_id,
    drop column total_qty;

alter table outbound_order_item
    change column qty planned_qty decimal(18, 2) not null default 0 comment '计划出库数量',
    add column line_no     int            not null default 1 comment '行号' after order_id,
    add column location_id bigint         null comment '来源库位ID' after material_id,
    add column shipped_qty decimal(18, 2) not null default 0 comment '已出库数量' after planned_qty,
    add unique key uk_outbound_item_line (order_id, line_no),
    add key idx_outbound_item_location_id (location_id);

-- =============================================
-- 变更记录（2026-04-17）：新增user表
-- =============================================

CREATE TABLE `user`
(
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `username`    VARCHAR(255) DEFAULT NULL COMMENT '用户名',
    `password`    VARCHAR(255) DEFAULT NULL COMMENT '登录密码',
    `name`        VARCHAR(255) DEFAULT NULL COMMENT '用户昵称',
    `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '用户头像',
    `profile`     TEXT         DEFAULT NULL COMMENT '用户简介',
    `role`        VARCHAR(100) DEFAULT NULL COMMENT '用户角色',
    `status`      INT          DEFAULT 0 COMMENT '账号状态，0=正常，1=停用',
    create_by       varchar(64)    not null default '' comment '创建人',
    create_time     datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by       varchar(64)    not null default '' comment '更新人',
    update_time     datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_username` (`username`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

INSERT INTO `user` (id,username,password,name,avatar,profile,role,status,create_time,update_time)
VALUES ( 1923456789012345678, 'admin', '$2a$10$Ziw/AnOoKNlnpj3J0.N.SO07DQlU8KhlBx9gtNNgDbqPWHJ/kgErS',
        '管理员', '', '系统管理员账户', 'ADMIN', 0, NOW(), NOW());

-- =============================================
-- 变更记录（2026-04-22）：WMS测试数据初始化
-- 变更原因：
-- 1) 提供本地/测试环境联调样例数据（主数据 + 单据 + 库存）
-- 2) 减少前后端联调时“无可选数据/无单据数据”的空场景成本
-- 3) 使用固定ID与幂等写法，支持重复执行
-- =============================================

-- 仓库测试数据
insert into warehouse (id, warehouse_code, warehouse_name, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000001, 'WH-GZ-01', '广州一号仓', 0, '测试仓库-华南', 'seed', now(), 'seed', now()),
       (2000000000000000002, 'WH-SH-01', '上海一号仓', 0, '测试仓库-华东', 'seed', now(), 'seed', now()),
       (2000000000000000003, 'WH-BJ-01', '北京一号仓', 1, '测试仓库-停用样例', 'seed', now(), 'seed', now())
on duplicate key update
    warehouse_name = values(warehouse_name),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

-- 库位测试数据
insert into location (id, warehouse_id, location_code, location_name, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000101, 2000000000000000001, 'L-GZ-A-01', '广州A区01位', 0, 'A区标准位', 'seed', now(), 'seed', now()),
       (2000000000000000102, 2000000000000000001, 'L-GZ-A-02', '广州A区02位', 0, 'A区标准位', 'seed', now(), 'seed', now()),
       (2000000000000000103, 2000000000000000002, 'L-SH-B-01', '上海B区01位', 0, 'B区标准位', 'seed', now(), 'seed', now()),
       (2000000000000000104, 2000000000000000002, 'L-SH-B-02', '上海B区02位', 1, '停用库位样例', 'seed', now(), 'seed', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    location_name = values(location_name),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

-- 物料测试数据
insert into material (id, material_code, material_name, specification, unit, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000201, 'MAT-BOLT-M8', '六角螺栓M8', 'M8*30', '个', 0, '紧固件', 'seed', now(), 'seed', now()),
       (2000000000000000202, 'MAT-NUT-M8', '六角螺母M8', 'M8', '个', 0, '紧固件', 'seed', now(), 'seed', now()),
       (2000000000000000203, 'MAT-PLATE-SUS', '不锈钢垫片', 'SUS304', '个', 0, '辅料', 'seed', now(), 'seed', now()),
       (2000000000000000204, 'MAT-LABEL-OLD', '旧版标签纸', '80*50', '卷', 1, '停用物料样例', 'seed', now(), 'seed', now())
on duplicate key update
    material_name = values(material_name),
    specification = values(specification),
    unit = values(unit),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

-- 入库单测试数据（1条已确认 + 1条草稿）
insert into inbound_order (id, order_no, warehouse_id, status, inbound_time, remark, create_by, create_time, update_by, update_time)
values (2000000000000000301, 'IN202604220001', 2000000000000000001, 1, now(), '测试入库单-已确认', 'seed', now(), 'seed', now()),
       (2000000000000000302, 'IN202604220002', 2000000000000000002, 0, null, '测试入库单-草稿', 'seed', now(), 'seed', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    status = values(status),
    inbound_time = values(inbound_time),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into inbound_order_item (id, order_id, line_no, material_id, location_id, planned_qty, received_qty, remark, create_by, create_time, update_by, update_time)
values (2000000000000000401, 2000000000000000301, 1, 2000000000000000201, 2000000000000000101, 120.00, 120.00, '螺栓入库', 'seed', now(), 'seed', now()),
       (2000000000000000402, 2000000000000000301, 2, 2000000000000000202, 2000000000000000102, 240.00, 240.00, '螺母入库', 'seed', now(), 'seed', now()),
       (2000000000000000403, 2000000000000000302, 1, 2000000000000000203, 2000000000000000103, 80.00, 0.00, '草稿明细样例', 'seed', now(), 'seed', now())
on duplicate key update
    material_id = values(material_id),
    location_id = values(location_id),
    planned_qty = values(planned_qty),
    received_qty = values(received_qty),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

-- 出库单测试数据（1条已确认 + 1条草稿）
insert into outbound_order (id, order_no, warehouse_id, status, outbound_time, remark, create_by, create_time, update_by, update_time)
values (2000000000000000501, 'OUT202604220001', 2000000000000000001, 1, now(), '测试出库单-已确认', 'seed', now(), 'seed', now()),
       (2000000000000000502, 'OUT202604220002', 2000000000000000002, 0, null, '测试出库单-草稿', 'seed', now(), 'seed', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    status = values(status),
    outbound_time = values(outbound_time),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into outbound_order_item (id, order_id, line_no, material_id, location_id, planned_qty, shipped_qty, remark, create_by, create_time, update_by, update_time)
values (2000000000000000601, 2000000000000000501, 1, 2000000000000000201, 2000000000000000101, 30.00, 30.00, '已确认出库明细', 'seed', now(), 'seed', now()),
       (2000000000000000602, 2000000000000000502, 1, 2000000000000000203, 2000000000000000103, 20.00, 0.00, '草稿出库明细', 'seed', now(), 'seed', now())
on duplicate key update
    material_id = values(material_id),
    location_id = values(location_id),
    planned_qty = values(planned_qty),
    shipped_qty = values(shipped_qty),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

-- 库存测试数据
insert into inventory (id, warehouse_id, location_id, material_id, quantity, locked_quantity, create_by, create_time, update_by, update_time)
values (2000000000000000701, 2000000000000000001, 2000000000000000101, 2000000000000000201, 90.00, 10.00, 'seed', now(), 'seed', now()),
       (2000000000000000702, 2000000000000000001, 2000000000000000102, 2000000000000000202, 240.00, 0.00, 'seed', now(), 'seed', now()),
       (2000000000000000703, 2000000000000000002, 2000000000000000103, 2000000000000000203, 60.00, 5.00, 'seed', now(), 'seed', now())
on duplicate key update
    quantity = values(quantity),
    locked_quantity = values(locked_quantity),
    update_by = values(update_by),
    update_time = values(update_time);

-- =============================================
-- 变更记录（2026-04-22）：引入区域模型（warehouse -> area -> location）
-- 变更原因：
-- 1) 当前仅有仓库与库位，缺少区域层，无法表达收货区/质检区/存储区等业务分区
-- 2) 支撑主数据联动口径：仓库 -> 区域 -> 库位，避免单据页跨仓跨区误选
-- 3) 为后续空间可视化与流程分区扩展提供结构基础
-- =============================================

-- 区域表
create table if not exists area
(
    id            bigint primary key comment '主键ID',
    warehouse_id  bigint       not null comment '所属仓库ID',
    area_code     varchar(32)  not null comment '区域编码',
    area_name     varchar(64)  not null comment '区域名称',
    area_type     varchar(32)  null comment '区域类型',
    status        tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark        varchar(255) not null default '' comment '备注',
    create_by     varchar(64)  not null default '' comment '创建人',
    create_time   datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)  not null default '' comment '更新人',
    update_time   datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_area_warehouse_code (warehouse_id, area_code),
    key idx_area_warehouse_id (warehouse_id)
) engine = InnoDB comment '区域表'
  collate = utf8mb4_unicode_ci;

-- 区域测试数据
insert into area (id, warehouse_id, area_code, area_name, area_type, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000151, 2000000000000000001, 'AR-GZ-REC', '广州收货区', 'RECEIVING', 0, '广州仓收货作业区', 'seed', now(), 'seed', now()),
       (2000000000000000152, 2000000000000000001, 'AR-GZ-STO', '广州存储区', 'STORAGE', 0, '广州仓标准存储区', 'seed', now(), 'seed', now()),
       (2000000000000000153, 2000000000000000002, 'AR-SH-STO', '上海存储区', 'STORAGE', 0, '上海仓标准存储区', 'seed', now(), 'seed', now()),
       (2000000000000000154, 2000000000000000002, 'AR-SH-QA', '上海质检区', 'QUALITY', 0, '上海仓质检区', 'seed', now(), 'seed', now()),
       (2000000000000000155, 2000000000000000003, 'AR-BJ-DIS', '北京停用区', 'DISABLED', 1, '停用仓示例区域', 'seed', now(), 'seed', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    area_name = values(area_name),
    area_type = values(area_type),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

alter table location
    add column area_id bigint null comment '所属区域ID' after warehouse_id;

update location
set area_id = case
                  when warehouse_id = 2000000000000000001 and location_code in ('L-GZ-A-01', 'L-GZ-A-02') then 2000000000000000152
                  when warehouse_id = 2000000000000000002 and location_code = 'L-SH-B-01' then 2000000000000000153
                  when warehouse_id = 2000000000000000002 and location_code = 'L-SH-B-02' then 2000000000000000154
                  else area_id
    end
where area_id is null;

alter table location
    modify column area_id bigint not null comment '所属区域ID',
    add key idx_location_area_id (area_id),
    add unique key uk_location_area_code (area_id, location_code);

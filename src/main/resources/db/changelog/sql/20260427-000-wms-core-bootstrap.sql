--liquibase formatted sql

--changeset berlin:20260427-000-wms-core-bootstrap labels:wms context:all
--comment: WMS 核心结构基线（全量建表，幂等）
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

create table if not exists location
(
    id            bigint primary key comment '主键ID',
    warehouse_id  bigint       not null comment '所属仓库ID',
    area_id       bigint       null comment '所属区域ID',
    location_code varchar(32)  not null comment '库位编码',
    location_name varchar(64)  not null comment '库位名称',
    status        tinyint      not null default 0 comment '状态（0正常 1停用）',
    remark        varchar(255) not null default '' comment '备注',
    create_by     varchar(64)  not null default '' comment '创建人',
    create_time   datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)  not null default '' comment '更新人',
    update_time   datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_location_code (location_code),
    unique key uk_location_area_code (area_id, location_code),
    key idx_location_warehouse_id (warehouse_id),
    key idx_location_area_id (area_id)
) engine = InnoDB comment '库位表'
  collate = utf8mb4_unicode_ci;

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

create table if not exists inbound_order
(
    id           bigint primary key comment '主键ID',
    order_no     varchar(32)  not null comment '入库单号',
    warehouse_id bigint       not null comment '仓库ID',
    status       tinyint      not null default 0 comment '状态（0草稿 1已确认）',
    inbound_time datetime     null comment '实际入库时间',
    remark       varchar(255) not null default '' comment '备注',
    create_by    varchar(64)  not null default '' comment '创建人',
    create_time  datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by    varchar(64)  not null default '' comment '更新人',
    update_time  datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_inbound_order_no (order_no),
    key idx_inbound_warehouse_id (warehouse_id)
) engine = InnoDB comment '入库单'
  collate = utf8mb4_unicode_ci;

create table if not exists inbound_order_item
(
    id           bigint primary key comment '主键ID',
    order_id     bigint         not null comment '入库单ID',
    line_no      int            not null default 1 comment '行号',
    material_id  bigint         not null comment '物料ID',
    location_id  bigint         null comment '目标库位ID',
    planned_qty  decimal(18, 2) not null default 0 comment '计划入库数量',
    received_qty decimal(18, 2) not null default 0 comment '已入库数量',
    remark       varchar(255)   not null default '' comment '备注',
    create_by    varchar(64)    not null default '' comment '创建人',
    create_time  datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by    varchar(64)    not null default '' comment '更新人',
    update_time  datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_inbound_item_line (order_id, line_no),
    key idx_inbound_item_order_id (order_id),
    key idx_inbound_item_material_id (material_id),
    key idx_inbound_item_location_id (location_id)
) engine = InnoDB comment '入库单明细'
  collate = utf8mb4_unicode_ci;

create table if not exists outbound_order
(
    id            bigint primary key comment '主键ID',
    order_no      varchar(32)  not null comment '出库单号',
    warehouse_id  bigint       not null comment '仓库ID',
    status        tinyint      not null default 0 comment '状态（0草稿 1已确认）',
    outbound_time datetime     null comment '出库时间',
    remark        varchar(255) not null default '' comment '备注',
    create_by     varchar(64)  not null default '' comment '创建人',
    create_time   datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)  not null default '' comment '更新人',
    update_time   datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_outbound_order_no (order_no),
    key idx_outbound_warehouse_id (warehouse_id)
) engine = InnoDB comment '出库单'
  collate = utf8mb4_unicode_ci;

create table if not exists outbound_order_item
(
    id          bigint primary key comment '主键ID',
    order_id    bigint         not null comment '出库单ID',
    line_no     int            not null default 1 comment '行号',
    material_id bigint         not null comment '物料ID',
    location_id bigint         null comment '来源库位ID',
    planned_qty decimal(18, 2) not null default 0 comment '计划出库数量',
    shipped_qty decimal(18, 2) not null default 0 comment '已出库数量',
    remark      varchar(255)   not null default '' comment '备注',
    create_by   varchar(64)    not null default '' comment '创建人',
    create_time datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)    not null default '' comment '更新人',
    update_time datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_outbound_item_line (order_id, line_no),
    key idx_outbound_item_order_id (order_id),
    key idx_outbound_item_material_id (material_id),
    key idx_outbound_item_location_id (location_id)
) engine = InnoDB comment '出库单明细'
  collate = utf8mb4_unicode_ci;

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

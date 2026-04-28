--liquibase formatted sql

--changeset berlin:20260428-002-inventory-adjustment labels:wms context:all
--comment: 新增库存调整单与明细，支持受控库存增减调整
create table if not exists inventory_adjustment
(
    id            bigint primary key comment '主键ID',
    order_no      varchar(32)  not null comment '调整单号',
    warehouse_id  bigint       not null comment '仓库ID',
    area_id       bigint       not null comment '区域ID',
    adjust_type   varchar(16)  not null comment '调整方向（INCREASE/DECREASE）',
    status        tinyint      not null default 0 comment '状态（0草稿 1已确认）',
    adjust_time   datetime     null comment '实际调整时间',
    adjust_reason varchar(128) not null comment '调整原因',
    remark        varchar(255) not null default '' comment '备注',
    create_by     varchar(64)  not null default '' comment '创建人',
    create_time   datetime     not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by     varchar(64)  not null default '' comment '更新人',
    update_time   datetime     not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_inventory_adjustment_order_no (order_no),
    key idx_inventory_adjustment_warehouse_id (warehouse_id),
    key idx_inventory_adjustment_area_id (area_id),
    key idx_inventory_adjustment_status (status)
) engine = InnoDB comment '库存调整单'
  collate = utf8mb4_unicode_ci;

create table if not exists inventory_adjustment_item
(
    id          bigint primary key comment '主键ID',
    order_id    bigint         not null comment '调整单ID',
    line_no     int            not null default 1 comment '行号',
    material_id bigint         not null comment '物料ID',
    location_id bigint         not null comment '库位ID',
    adjust_qty  decimal(18, 2) not null default 0 comment '调整数量',
    remark      varchar(255)   not null default '' comment '备注',
    create_by   varchar(64)    not null default '' comment '创建人',
    create_time datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by   varchar(64)    not null default '' comment '更新人',
    update_time datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_inventory_adjustment_item_line (order_id, line_no),
    key idx_inventory_adjustment_item_order_id (order_id),
    key idx_inventory_adjustment_item_material_id (material_id),
    key idx_inventory_adjustment_item_location_id (location_id)
) engine = InnoDB comment '库存调整单明细'
  collate = utf8mb4_unicode_ci;

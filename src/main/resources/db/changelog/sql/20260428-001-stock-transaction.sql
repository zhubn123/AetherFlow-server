--liquibase formatted sql

--changeset berlin:20260428-001-stock-transaction labels:wms context:all
--comment: 新增库存流水表，用于记录统一库存变动入口产生的库存变更明细
create table if not exists stock_transaction
(
    id           bigint primary key comment '主键ID',
    biz_type     varchar(32)    not null comment '业务类型',
    biz_id       bigint         not null comment '业务单据ID',
    material_id  bigint         not null comment '物料ID',
    warehouse_id bigint         not null comment '仓库ID',
    area_id      bigint         null comment '区域ID',
    location_id  bigint         not null comment '库位ID',
    change_qty   decimal(18, 2) not null comment '变动数量（正数入库，负数出库）',
    before_qty   decimal(18, 2) not null default 0 comment '变动前库存',
    after_qty    decimal(18, 2) not null default 0 comment '变动后库存',
    operator_id  bigint         null comment '操作人ID',
    operate_time datetime       not null default CURRENT_TIMESTAMP comment '操作时间',
    remark       varchar(255)   not null default '' comment '备注',
    create_by    varchar(64)    not null default '' comment '创建人',
    create_time  datetime       not null default CURRENT_TIMESTAMP comment '创建时间',
    update_by    varchar(64)    not null default '' comment '更新人',
    update_time  datetime       not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    key idx_stock_tx_biz (biz_type, biz_id),
    key idx_stock_tx_material_id (material_id),
    key idx_stock_tx_warehouse_id (warehouse_id),
    key idx_stock_tx_area_id (area_id),
    key idx_stock_tx_location_id (location_id),
    key idx_stock_tx_operate_time (operate_time)
) engine = InnoDB comment '库存流水表'
  collate = utf8mb4_unicode_ci;

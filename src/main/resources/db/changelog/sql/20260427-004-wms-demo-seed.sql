--liquibase formatted sql

--changeset berlin:20260427-004-wms-demo-seed labels:wms context:all
--comment: WMS 演示数据（主数据 + 单据 + 库存，幂等）
insert into warehouse (id, warehouse_code, warehouse_name, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000001, 'WH-GZ-01', '广州一号仓', 0, '演示仓库-华南', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000002, 'WH-SH-01', '上海一号仓', 0, '演示仓库-华东', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000003, 'WH-BJ-01', '北京一号仓', 1, '演示仓库-停用样例', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    warehouse_name = values(warehouse_name),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into area (id, warehouse_id, area_code, area_name, area_type, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000151, 2000000000000000001, 'AR-GZ-REC', '广州收货区', 'RECEIVING', 0, '广州仓收货作业区', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000152, 2000000000000000001, 'AR-GZ-STO', '广州存储区', 'STORAGE', 0, '广州仓标准存储区', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000153, 2000000000000000002, 'AR-SH-STO', '上海存储区', 'STORAGE', 0, '上海仓标准存储区', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000154, 2000000000000000002, 'AR-SH-QA', '上海质检区', 'QUALITY', 0, '上海仓质检区', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000155, 2000000000000000003, 'AR-BJ-DIS', '北京停用区', 'DISABLED', 1, '停用仓示例区域', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    area_name = values(area_name),
    area_type = values(area_type),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into location (id, warehouse_id, area_id, location_code, location_name, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000101, 2000000000000000001, 2000000000000000152, 'L-GZ-A-01', '广州A区01位', 0, 'A区标准位', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000102, 2000000000000000001, 2000000000000000152, 'L-GZ-A-02', '广州A区02位', 0, 'A区标准位', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000103, 2000000000000000002, 2000000000000000153, 'L-SH-B-01', '上海B区01位', 0, 'B区标准位', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000104, 2000000000000000002, 2000000000000000154, 'L-SH-B-02', '上海B区02位', 1, '停用库位样例', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    area_id = values(area_id),
    location_name = values(location_name),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into material (id, material_code, material_name, specification, unit, status, remark, create_by, create_time, update_by, update_time)
values (2000000000000000201, 'MAT-BOLT-M8', '六角螺栓M8', 'M8*30', '个', 0, '紧固件', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000202, 'MAT-NUT-M8', '六角螺母M8', 'M8', '个', 0, '紧固件', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000203, 'MAT-PLATE-SUS', '不锈钢垫片', 'SUS304', '个', 0, '辅料', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000204, 'MAT-LABEL-OLD', '旧版标签纸', '80*50', '卷', 1, '停用物料样例', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    material_name = values(material_name),
    specification = values(specification),
    unit = values(unit),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into inbound_order (id, order_no, warehouse_id, status, inbound_time, remark, create_by, create_time, update_by, update_time)
values (2000000000000000301, 'IN202604220001', 2000000000000000001, 1, now(), '演示入库单-已确认', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000302, 'IN202604220002', 2000000000000000002, 0, null, '演示入库单-草稿', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    status = values(status),
    inbound_time = values(inbound_time),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into inbound_order_item (id, order_id, line_no, material_id, location_id, planned_qty, received_qty, remark, create_by, create_time, update_by, update_time)
values (2000000000000000401, 2000000000000000301, 1, 2000000000000000201, 2000000000000000101, 120.00, 120.00, '螺栓入库', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000402, 2000000000000000301, 2, 2000000000000000202, 2000000000000000102, 240.00, 240.00, '螺母入库', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000403, 2000000000000000302, 1, 2000000000000000203, 2000000000000000103, 80.00, 0.00, '草稿明细样例', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    material_id = values(material_id),
    location_id = values(location_id),
    planned_qty = values(planned_qty),
    received_qty = values(received_qty),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into outbound_order (id, order_no, warehouse_id, status, outbound_time, remark, create_by, create_time, update_by, update_time)
values (2000000000000000501, 'OUT202604220001', 2000000000000000001, 1, now(), '演示出库单-已确认', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000502, 'OUT202604220002', 2000000000000000002, 0, null, '演示出库单-草稿', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    warehouse_id = values(warehouse_id),
    status = values(status),
    outbound_time = values(outbound_time),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into outbound_order_item (id, order_id, line_no, material_id, location_id, planned_qty, shipped_qty, remark, create_by, create_time, update_by, update_time)
values (2000000000000000601, 2000000000000000501, 1, 2000000000000000201, 2000000000000000101, 30.00, 30.00, '已确认出库明细', 'liquibase', now(), 'liquibase', now()),
       (2000000000000000602, 2000000000000000502, 1, 2000000000000000203, 2000000000000000103, 20.00, 0.00, '草稿出库明细', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    material_id = values(material_id),
    location_id = values(location_id),
    planned_qty = values(planned_qty),
    shipped_qty = values(shipped_qty),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into inventory (id, warehouse_id, location_id, material_id, quantity, locked_quantity, create_by, create_time, update_by, update_time)
values (2000000000000000701, 2000000000000000001, 2000000000000000101, 2000000000000000201, 90.00, 10.00, 'liquibase', now(), 'liquibase', now()),
       (2000000000000000702, 2000000000000000001, 2000000000000000102, 2000000000000000202, 240.00, 0.00, 'liquibase', now(), 'liquibase', now()),
       (2000000000000000703, 2000000000000000002, 2000000000000000103, 2000000000000000203, 60.00, 5.00, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    quantity = values(quantity),
    locked_quantity = values(locked_quantity),
    update_by = values(update_by),
    update_time = values(update_time);

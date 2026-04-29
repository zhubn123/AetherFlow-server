--liquibase formatted sql

--changeset berlin:20260429-002-user-permission-seed labels:user-domain context:all
--comment: 用户域权限基线数据与角色权限映射（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001001, 'wms:option:view', 'WMS 选项查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001002, 'wms:warehouse:view', '仓库查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001003, 'wms:warehouse:manage', '仓库管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001004, 'wms:area:view', '区域查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001005, 'wms:area:manage', '区域管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001006, 'wms:location:view', '库位查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001007, 'wms:location:manage', '库位管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001008, 'wms:material:view', '物料查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001009, 'wms:material:manage', '物料管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001010, 'wms:inventory:view', '库存查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001011, 'wms:stock-transaction:view', '库存流水查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001012, 'wms:inbound-order:view', '入库单查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001013, 'wms:inbound-order:manage', '入库单管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001014, 'wms:outbound-order:view', '出库单查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001015, 'wms:outbound-order:manage', '出库单管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001016, 'wms:inventory-adjustment:view', '库存调整单查看', 'wms', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001017, 'wms:inventory-adjustment:manage', '库存调整单管理', 'wms', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values
       (1930000000000002001, 1930000000000000001, 1930000000000001001, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002002, 1930000000000000001, 1930000000000001002, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002003, 1930000000000000001, 1930000000000001003, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002004, 1930000000000000001, 1930000000000001004, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002005, 1930000000000000001, 1930000000000001005, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002006, 1930000000000000001, 1930000000000001006, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002007, 1930000000000000001, 1930000000000001007, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002008, 1930000000000000001, 1930000000000001008, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002009, 1930000000000000001, 1930000000000001009, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002010, 1930000000000000001, 1930000000000001010, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002011, 1930000000000000001, 1930000000000001011, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002012, 1930000000000000001, 1930000000000001012, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002013, 1930000000000000001, 1930000000000001013, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002014, 1930000000000000001, 1930000000000001014, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002015, 1930000000000000001, 1930000000000001015, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002016, 1930000000000000001, 1930000000000001016, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002017, 1930000000000000001, 1930000000000001017, 'liquibase', now(), 'liquibase', now()),

       (1930000000000002101, 1930000000000000002, 1930000000000001001, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002102, 1930000000000000002, 1930000000000001002, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002103, 1930000000000000002, 1930000000000001004, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002104, 1930000000000000002, 1930000000000001006, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002105, 1930000000000000002, 1930000000000001008, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002106, 1930000000000000002, 1930000000000001010, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002107, 1930000000000000002, 1930000000000001011, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002108, 1930000000000000002, 1930000000000001012, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002109, 1930000000000000002, 1930000000000001013, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002110, 1930000000000000002, 1930000000000001014, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002111, 1930000000000000002, 1930000000000001015, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002112, 1930000000000000002, 1930000000000001016, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002113, 1930000000000000002, 1930000000000001017, 'liquibase', now(), 'liquibase', now()),

       (1930000000000002201, 1930000000000000003, 1930000000000001001, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002202, 1930000000000000003, 1930000000000001002, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002203, 1930000000000000003, 1930000000000001004, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002204, 1930000000000000003, 1930000000000001006, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002205, 1930000000000000003, 1930000000000001008, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002206, 1930000000000000003, 1930000000000001010, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002207, 1930000000000000003, 1930000000000001011, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002208, 1930000000000000003, 1930000000000001012, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002209, 1930000000000000003, 1930000000000001014, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002210, 1930000000000000003, 1930000000000001016, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

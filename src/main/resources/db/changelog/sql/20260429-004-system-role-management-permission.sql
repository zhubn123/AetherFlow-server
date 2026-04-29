--liquibase formatted sql

--changeset berlin:20260429-004-system-role-management-permission labels:user-domain context:all
--comment: 角色管理与权限目录权限码（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001020, 'system:role:view', '系统角色查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001021, 'system:role:manage', '系统角色管理', 'system', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001022, 'system:permission:view', '系统权限目录查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002020, 1930000000000000001, 1930000000000001020, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002021, 1930000000000000001, 1930000000000001021, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002022, 1930000000000000001, 1930000000000001022, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

--liquibase formatted sql

--changeset berlin:20260429-003-system-user-management-permission labels:user-domain context:all
--comment: 用户管理后台权限码与管理员默认映射（幂等）
insert into sys_permission (id, perm_key, perm_name, module, action, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000001018, 'system:user:view', '系统用户查看', 'system', 'view', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now()),
       (1930000000000001019, 'system:user:manage', '系统用户管理', 'system', 'manage', 0, '系统内置权限', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    perm_name = values(perm_name),
    module = values(module),
    action = values(action),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time)
values (1930000000000002018, 1930000000000000001, 1930000000000001018, 'liquibase', now(), 'liquibase', now()),
       (1930000000000002019, 1930000000000000001, 1930000000000001019, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

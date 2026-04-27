--liquibase formatted sql

--changeset berlin:20260427-002-user-domain-seed labels:user-domain context:all
--comment: 用户域角色与管理员基线数据（幂等）
insert into sys_role (id, role_key, role_name, status, remark, create_by, create_time, update_by, update_time)
values (1930000000000000001, 'admin', '系统管理员', 0, '系统内置角色', 'liquibase', now(), 'liquibase', now()),
       (1930000000000000002, 'operator', '业务操作员', 0, '系统内置角色', 'liquibase', now(), 'liquibase', now()),
       (1930000000000000003, 'viewer', '只读访客', 0, '系统内置角色', 'liquibase', now(), 'liquibase', now())
on duplicate key update
    role_name = values(role_name),
    status = values(status),
    remark = values(remark),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_user (id, username, password_hash, nickname, email, phone, status, login_fail_count, lock_until, last_login_time,
                      create_by, create_time, update_by, update_time)
values (1930000000000000101, 'admin', '$2a$10$Ziw/AnOoKNlnpj3J0.N.SO07DQlU8KhlBx9gtNNgDbqPWHJ/kgErS',
        '管理员', 'admin@example.com', null, 0, 0, null, now(), 'liquibase', now(), 'liquibase', now())
on duplicate key update
    password_hash = values(password_hash),
    nickname = values(nickname),
    email = values(email),
    status = values(status),
    update_by = values(update_by),
    update_time = values(update_time);

insert into sys_user_role (id, user_id, role_id, create_by, create_time, update_by, update_time)
values (1930000000000000201, 1930000000000000101, 1930000000000000001, 'liquibase', now(), 'liquibase', now())
on duplicate key update
    update_by = values(update_by),
    update_time = values(update_time);

# Liquibase 线上变更说明

## 1. 目标

- 将线上数据库结构变更纳入版本化管理。
- 避免手工执行 SQL 造成“环境漂移”。

## 2. 接入现状

- 已引入依赖：`liquibase-core`
- 启动配置：
  - `spring.liquibase.enabled=true`
  - `spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.yaml`
- 主 changelog：`src/main/resources/db/changelog/db.changelog-master.yaml`
- 变更 SQL 目录：`src/main/resources/db/changelog/sql`
- 已接入“未知阶段兼容”策略：
  - `20260427-000-wms-core-bootstrap.sql`：WMS 核心表幂等建表
  - `20260427-000a-wms-legacy-repair.yaml`：历史结构差异自动修复（按前置校验执行）

## 3. 变更流程

1. 新建一个递增变更文件（不要改历史文件）：
   - 例：`20260501-001-add-xxx.sql`
2. 按 Liquibase formatted SQL 规范编写：
   - 以 `--liquibase formatted sql` 开头
   - 添加 `--changeset author:id`
3. 在 `db.changelog-master.yaml` 中追加 `include`。
4. 本地启动应用或执行测试，确认变更可执行。
5. 合并代码后，线上服务启动会自动执行未运行过的 changeset。

## 4. 规则

- 历史 changeset 不修改、不删除。
- SQL 尽量幂等（`if not exists` / `on duplicate key update`）。
- 高风险变更（删列、改类型）必须先走备份和灰度策略。
- 线上不再以手工 SQL 作为主发布方式。

## 5. 备注

- `sql/aether-flow.sql` 已降级为历史归档，不再作为发布入口。
- 线上结构以 Liquibase 为唯一事实来源。

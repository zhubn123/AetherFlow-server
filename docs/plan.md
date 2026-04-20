# AetherFlow WMS Plan

# 阶段一
## 阶段一目标

先完成 WMS 第一批核心业务闭环，顺序为：

1. 仓库管理
2. 库位管理
3. 物料管理
4. 入库管理
5. 出库管理
6. 库存查询
7. 登录与权限（阶段一收尾接入）

---

## 阶段一：要完成的模块

业务模块：

- `wms/warehouse`
- `wms/location`
- `wms/material`
- `wms/inbound`
- `wms/outbound`
- `wms/inventory`

收尾模块：

- `system/auth`
- `system/user`
- `system/role`（最小 RBAC）

---

## 阶段一：业务关系与流程口径（整体到局部）

整体关系：

- 仓库是仓储容器。
- 库位属于仓库，是仓库内部存储位置。
- 物料是被管理的货物种类。
- 入库单和出库单记录库存变化过程。
- 库存查询展示最终库存结果。

核心口径：

- 一个仓库可有多个库位。
- 一个库位只属于一个仓库。
- 一个物料可分布在多个仓库和库位。
- 入库/出库都采用“单头 + 明细”结构。
- 库存唯一维度：`warehouse + location + material`。

流程口径：

1. 建仓库、库位、物料主数据。
2. 创建并确认入库单，库存增加。
3. 创建并确认出库单，库存校验后扣减。
4. 在库存查询查看实时结果。
5. 阶段一末统一接入登录鉴权。

关键约束：

- 出库前必须校验库存，库存不足禁止出库。
- 仓库停用前检查是否有库存或在途单据。
- 库位停用前检查是否有库存。
- 物料停用后禁止新增入出库明细。

---

## 阶段一：数据模型

第一批核心表：

- `warehouse`（仓库）
- `location`（库位）
- `material`（物料）
- `inbound_order`（入库单）
- `inbound_order_item`（入库单明细）
- `outbound_order`（出库单）
- `outbound_order_item`（出库单明细）
- `inventory`（库存）

阶段一收尾补充表：

- `sys_user`
- `sys_role`
- `sys_user_role`
- `sys_menu`（可简化）
- `sys_role_menu`（可简化）

---

## 阶段一：功能清单

仓库管理：

- 仓库新增、编辑、列表查询、状态启停

库位管理：

- 库位新增、编辑、列表查询、按仓库筛选

物料管理：

- 物料新增、编辑、列表查询、状态启停

入库管理：

- 入库单新增、明细维护、入库确认、列表查询

出库管理：

- 出库单新增、明细维护、出库确认、列表查询

库存查询：

- 按仓库/库位/物料维度查询库存

登录与权限（阶段一收尾）：

- 登录、登出、最小角色模型、接口鉴权

---

## 阶段一：接口规划

仓库：

- `GET /api/warehouses`
- `GET /api/warehouses/{id}`
- `POST /api/warehouses`
- `PUT /api/warehouses/{id}`

库位：

- `GET /api/locations`
- `GET /api/locations/{id}`
- `POST /api/locations`
- `PUT /api/locations/{id}`

物料：

- `GET /api/materials`
- `GET /api/materials/{id}`
- `POST /api/materials`
- `PUT /api/materials/{id}`

入库：

- `GET /api/inbound-orders`
- `GET /api/inbound-orders/{id}`
- `POST /api/inbound-orders`
- `POST /api/inbound-orders/{id}/confirm`

出库：

- `GET /api/outbound-orders`
- `GET /api/outbound-orders/{id}`
- `POST /api/outbound-orders`
- `POST /api/outbound-orders/{id}/confirm`

库存：

- `GET /api/stocks`

收尾鉴权：

- `POST /api/auth/login`
- `POST /api/auth/logout`

---

## 阶段一验收标准

- 主数据（仓库/库位/物料）可稳定维护。
- 入库可正确增加库存。
- 出库可正确扣减库存并校验库存不足。
- 库存查询能按核心维度准确展示。
- 收尾后未登录请求受限，登录后可访问业务接口。


# 阶段...
## dict表，各种类型可配置、前端提供

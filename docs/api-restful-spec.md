# 接口命名与 RESTful 风格规范

## 1. 目标

这份规范用于统一 `AetherFlow-server` 的接口路径风格，降低后续接口数量变多后的维护成本。

当前项目统一采用：

- RESTful 风格
- 资源导向命名
- `/api` 作为统一前缀

## 2. 基本规则

### 2.1 路径统一使用小写

正确：

- `/api/users`
- `/api/warehouses`

错误：

- `/api/Users`
- `/api/getUsers`

### 2.2 路径统一使用中划线或纯英文单词

正确：

- `/api/inbound-orders`
- `/api/stock-records`

不推荐：

- `/api/inbound_orders`
- `/api/stockRecords`

### 2.3 资源名优先使用复数

正确：

- `/api/users`
- `/api/materials`
- `/api/warehouses`

### 2.4 路径中不要出现动作动词

正确：

- `GET /api/users`
- `POST /api/warehouses`

错误：

- `/api/getUsers`
- `/api/addWarehouse`
- `/api/deleteMaterial`
- `/api/updateStock`

## 3. HTTP 方法约定

### 查询列表

- `GET /api/users`
- `GET /api/warehouses`

### 查询详情

- `GET /api/users/{id}`
- `GET /api/materials/{id}`

### 创建资源

- `POST /api/materials`
- `POST /api/inbound-orders`

### 全量更新

- `PUT /api/materials/{id}`

### 局部更新

- `PATCH /api/materials/{id}`

### 删除资源

- `DELETE /api/materials/{id}`

## 4. 子资源路径

当一个资源明显属于另一个资源时，允许使用层级路径。

示例：

- `/api/warehouses/{warehouseId}/locations`
- `/api/inbound-orders/{orderId}/items`

规则：

- 层级不宜过深
- 一般控制在 2 到 3 层内

## 5. 模块接口建议

### system 模块

- `/api/auth/login`
- `/api/auth/logout`
- `/api/users`
- `/api/roles`
- `/api/menus`

说明：

- `auth` 属于动作型模块，可以保留语义化路径
- 登录/登出不强行机械做成纯资源 CRUD

### wms 模块

- `/api/warehouses`
- `/api/locations`
- `/api/materials`
- `/api/inbound-orders`
- `/api/outbound-orders`
- `/api/stocks`
- `/api/inventory-checks`

## 6. 允许的例外

以下情况允许不是纯资源名：

- `/api/auth/login`
- `/api/auth/logout`
- `/api/health`
- `/api/docs/*` 这类框架或监控接口

这些路径本质上属于：

- 鉴权动作
- 监控或系统健康检查
- 框架内置能力

## 7. 当前项目检查规则

启动时会对 Controller 路径做轻量检查，并输出 warning。

当前检查重点：

- 路径是否以 `/api` 开头
- 路径是否包含大写字母
- 路径是否包含下划线
- 路径是否使用典型动词前缀

注意：

- 当前只做告警
- 不阻止项目启动

## 8. 一句话标准

如果一个接口路径看起来像“方法名”，那大概率就不够 RESTful；
如果它看起来像“资源路径”，通常方向就是对的。

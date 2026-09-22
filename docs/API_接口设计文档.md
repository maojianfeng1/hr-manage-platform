# 人力资源管理服务平台 接口设计文档

> 版本：V1.0  
> 文档类型：API 设计规范（后端 REST 接口）

---

## 1. 接口规范

### 1.1 基础信息
- **Base URL**：`http://{host}:{port}/api`
- **请求/响应格式**：`application/json`
- **字符编码**：UTF-8

### 1.2 鉴权方式
系统采用 JWT（HS256）无状态鉴权：
1. 调用 `POST /api/auth/login` 获取 `token`。
2. 后续请求在 Header 携带：`Authorization: Bearer {token}`。
3. 拦截器解析 Token 写入线程上下文（用户ID、员工ID、角色、权限集）。

### 1.3 统一响应结构
所有接口返回统一封装：

```json
{
  "code": 200,
  "msg": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| code | int | 业务状态码，200 成功；401 未登录；403 无权限；500 服务异常 |
| msg | string | 提示信息 |
| data | object | 业务数据负载 |

### 1.4 分页约定
列表接口统一返回：

```json
{
  "code": 200,
  "data": {
    "records": [ ],
    "total": 100,
    "size": 10,
    "current": 1
  }
}
```

### 1.5 权限标识约定
接口权限由后端 `@RequiresPermission("xxx:view")` 注解声明，前端 `hasPerm` 同步控制显隐。权限码与菜单 `perms` 字段一一对应。

---

## 2. 鉴权模块 `/api/auth`

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/login` | POST | 无 | 账号密码登录，返回 token 与用户信息 |
| `/info` | GET | 无（需登录） | 获取当前登录用户信息、角色、权限、菜单 |
| `/logout` | POST | 无（需登录） | 注销登录 |

**登录请求示例**：
```json
POST /api/auth/login
{ "username": "admin", "password": "123456" }
```
**响应**：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": { "id": 1, "username": "admin", "roles": ["admin"], "permissions": ["system:user:view", "emp:view"] }
  }
}
```

---

## 3. 系统管理

### 3.1 用户管理 `/api/system/user`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/page` | GET | `system:user:view` | 用户分页列表 |
| `/{id}` | GET | `system:user:view` | 用户详情 |
| `/` | POST | `system:user:add` | 新增用户 |
| `/` | PUT | `system:user:edit` | 修改用户 |
| `/{id}` | DELETE | `system:user:edit` | 删除用户（逻辑删除） |
| `/{id}/reset-pwd` | POST | `system:user:edit` | 重置密码 |

### 3.2 角色管理 `/api/system/role`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/list` | GET | `system:role:view` | 角色列表 |
| `/{id}` | GET | `system:role:view` | 角色详情 |
| `/` | POST | `system:role:edit` | 新增角色 |
| `/` | PUT | `system:role:edit` | 修改角色 |
| `/{id}` | DELETE | `system:role:edit` | 删除角色 |
| `/{id}/menus` | POST | `system:role:edit` | 为角色分配菜单/权限 |

### 3.3 菜单管理 `/api/menu`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/user-menus` | GET | 需登录 | 当前用户菜单树（侧边栏渲染） |
| `/tree` | GET | `system:menu:view` | 全部菜单树（角色授权勾选） |
| `/list` | GET | `system:menu:view` | 菜单列表（管理页） |
| `/save` | POST | `system:menu:edit` | 新增菜单 |
| `/update` | PUT | `system:menu:edit` | 修改菜单 |
| `/remove/{id}` | DELETE | `system:menu:edit` | 删除菜单（含子节点校验） |

---

## 4. 组织管理

### 4.1 部门管理 `/api/org/dept`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/page` | GET | `org:dept:view` | 部门分页 |
| `/list` | GET | `org:dept:view` | 部门树列表 |
| `/{id}` | GET | `org:dept:view` | 部门详情 |
| `/` | POST | `org:dept:edit` | 新增部门 |
| `/` | PUT | `org:dept:edit` | 修改部门 |
| `/{id}` | DELETE | `org:dept:edit` | 删除部门 |

### 4.2 岗位管理 `/api/org/post`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/page` | GET | `org:post:view` | 岗位分页 |
| `/{id}` | GET | `org:post:view` | 岗位详情 |
| `/` | POST | `org:post:edit` | 新增岗位 |
| `/` | PUT | `org:post:edit` | 修改岗位 |
| `/{id}` | DELETE | `org:post:edit` | 删除岗位 |

---

## 5. 员工管理 `/api/employee`

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/page` | GET | `emp:view` | 员工档案分页 |
| `/{id}` | GET | `emp:view` | 员工详情（越权校验，见 §8） |
| `/` | POST | `emp:edit` | 新增员工 |
| `/` | PUT | `emp:edit` | 修改员工 |
| `/{id}` | DELETE | `emp:edit` | 删除员工 |

---

## 6. 考勤管理 `/api/attendance`

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/page` | GET | `att:view` | 考勤记录分页 |
| `/{id}` | GET | `att:view` | 考勤详情（越权校验） |
| `/` | POST | `att:edit` | 录入考勤 |
| `/` | PUT | `att:edit` | 修改考勤 |
| `/{id}` | DELETE | `att:edit` | 删除考勤 |

---

## 7. 薪酬管理

### 7.1 薪资档案 `/api/salary/standard`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/page` | GET | `sal:view` | 薪资档案分页 |
| `/{employeeId}` | GET | `sal:view` | 按员工查询（越权校验） |
| `/` | POST | `sal:standard:edit` | 新增/更新薪资档案 |

### 7.2 工资单 `/api/salary/payroll`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/page` | GET | `sal:view` | 工资单分页 |
| `/{id}` | GET | `sal:view` | 工资单详情（越权校验） |
| `/generate` | POST | `sal:payroll:edit` | 按月份生成工资单 |
| `/{id}` | DELETE | `sal:payroll:edit` | 作废工资单 |

---

## 8. 个人中心（行级权限）`/api/personal`

> 本模块接口**不声明 `@RequiresPermission`**，依赖 `UserContext` 中的 `empId` 实现行级数据隔离，普通员工仅可访问本人数据。

| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/my-info` | GET | 需登录 | 当前员工本人档案 |
| `/my-attendance` | GET | 需登录 | 当前员工本人考勤记录 |
| `/my-payroll` | GET | 需登录 | 当前员工本人工资单 |

---

## 9. 健康检查 `/api/health`
| 接口 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/` | GET | 无 | 服务健康探测（部署探针用） |

---

## 10. 错误码

| code | 含义 | 处理建议 |
|---|---|---|
| 200 | 成功 | — |
| 401 | 未登录 / Token 失效 | 跳转登录页重新获取 Token |
| 403 | 无操作权限 | 前端隐藏入口，后端直接拦截 |
| 400 | 参数校验失败 | 提示具体字段错误 |
| 500 | 服务内部异常 | 查看服务端日志 |

---

## 11. 安全设计要点
1. **密码安全**：数据库仅存储 BCrypt 哈希，明文密码不落库。
2. **接口鉴权**：业务接口声明权限注解，拦截器统一校验；未声明注解的内部接口（如 `/auth/login`、`/personal/*`）由业务层保证安全。
3. **行级权限**：个人中心以 `empId` 强制过滤，杜绝越权读取他人敏感数据（详见测试用例文档 §3.2）。
4. **防御性校验**：敏感详情接口（员工/考勤/薪酬）对普通员工角色做归属校验，非本人数据返回业务异常。

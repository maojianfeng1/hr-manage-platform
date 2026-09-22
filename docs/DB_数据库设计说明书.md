# 人力资源管理服务平台 数据库设计说明书

> 版本：V1.0  
> 数据库：MySQL 8.0.34  
> 字符集：utf8mb4 / utf8mb4_general_ci  
> 库名：`hr_manage`

---

## 1. 设计约定

1. **审计字段**：所有业务表统一包含 `create_time`、`update_time`、`is_deleted` 三个审计字段（`is_deleted` 为逻辑删除标记，0 有效 / 1 失效）。
2. **金额类型**：统一使用 `DECIMAL(10,2)`。
3. **日期/时间**：日期用 `DATE`，时刻用 `TIME` / `DATETIME`。
4. **状态/枚举**：使用 `TINYINT` 或 `VARCHAR(20)`，由业务层常量解释。
5. **外键策略**：不建立物理外键，采用逻辑外键（字段约定 + 应用层控制），便于数据维护与级联逻辑自定义。
6. **主键**：统一 `BIGINT AUTO_INCREMENT`。

---

## 2. 数据表清单（共 12 张）

| 序号 | 表名 | 中文名 | 分类 |
|---|---|---|---|
| 1 | `sys_user` | 系统用户表 | 权限（RBAC） |
| 2 | `sys_role` | 角色表 | 权限（RBAC） |
| 3 | `sys_menu` | 菜单权限表 | 权限（RBAC） |
| 4 | `sys_user_role` | 用户角色关联表 | 权限（RBAC） |
| 5 | `sys_role_menu` | 角色菜单关联表 | 权限（RBAC） |
| 6 | `hr_department` | 部门表 | 组织 |
| 7 | `hr_position` | 岗位表 | 组织 |
| 8 | `hr_employee` | 员工档案表 | 人事主数据 |
| 9 | `hr_attendance` | 考勤记录表 | 考勤 |
| 10 | `hr_salary_standard` | 薪资档案表 | 薪酬 |
| 11 | `hr_payroll` | 工资单表 | 薪酬 |
| 12 | `sys_login_log` | 登录日志表 | 审计 |

---

## 3. 表结构详述

### 3.1 系统用户表 `sys_user`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| username | VARCHAR(50) | 唯一 | 登录账号 |
| password | VARCHAR(100) | 非空 | BCrypt 密码哈希 |
| real_name | VARCHAR(50) | | 真实姓名 |
| avatar | VARCHAR(255) | | 头像 URL |
| email | VARCHAR(100) | | 邮箱 |
| phone | VARCHAR(20) | | 手机号 |
| status | TINYINT | 默认1 | 1启用 / 0停用 |
| employee_id | BIGINT | 索引 | 关联 `hr_employee.id`，普通员工必填 |
| last_login_time | DATETIME | | 最后登录时间 |

### 3.2 角色表 `sys_role`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| role_key | VARCHAR(50) | 唯一 | 角色标识（admin/hr/employee） |
| role_name | VARCHAR(50) | 非空 | 角色名称 |
| remark | VARCHAR(200) | | 备注 |

### 3.3 菜单权限表 `sys_menu`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| parent_id | BIGINT | 默认0 | 父菜单ID，0为顶级 |
| menu_name | VARCHAR(50) | 非空 | 菜单名称 |
| menu_type | TINYINT | 默认1 | 1菜单 / 2按钮（权限点） |
| perms | VARCHAR(100) | | 权限标识（如 system:user:view） |
| path | VARCHAR(200) | | 前端路由 |
| component | VARCHAR(200) | | 前端组件 |
| icon | VARCHAR(50) | | 图标 |
| sort | INT | 默认0 | 排序 |

### 3.4 用户角色关联 `sys_user_role`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| user_id | BIGINT | 联合唯一 | 用户ID |
| role_id | BIGINT | 联合唯一 | 角色ID |

### 3.5 角色菜单关联 `sys_role_menu`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| role_id | BIGINT | 联合唯一 | 角色ID |
| menu_id | BIGINT | 联合唯一 | 菜单ID |

### 3.6 部门表 `hr_department`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| dept_name | VARCHAR(50) | 非空 | 部门名称 |
| parent_id | BIGINT | 默认0 | 上级部门，0为顶级（树形） |
| dept_code | VARCHAR(50) | | 部门编码 |
| leader_id | BIGINT | | 负责人（关联 `hr_employee.id`） |
| sort | INT | | 排序 |
| status | TINYINT | 默认1 | 1启用 / 0停用 |

### 3.7 岗位表 `hr_position`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| post_name | VARCHAR(50) | 非空 | 岗位名称 |
| post_code | VARCHAR(50) | | 岗位编码 |
| dept_id | BIGINT | 默认0 | 所属部门 |
| headcount | INT | 默认0 | 编制人数 |
| remark | VARCHAR(200) | | 岗位描述 |
| status | TINYINT | 默认1 | 1启用 / 0停用 |

### 3.8 员工档案表 `hr_employee`（业务主表）
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| emp_code | VARCHAR(30) | 唯一 | 工号 |
| name | VARCHAR(50) | 非空 | 姓名 |
| gender | TINYINT | 默认1 | 1男 / 2女 |
| phone | VARCHAR(20) | | 手机号 |
| email | VARCHAR(100) | | 邮箱 |
| id_card | VARCHAR(20) | | 身份证号 |
| dept_id | BIGINT | 默认0 | 部门 |
| post_id | BIGINT | 默认0 | 岗位 |
| job_level | VARCHAR(20) | | 职级 |
| entry_date | DATE | | 入职日期 |
| regular_date | DATE | | 转正日期 |
| status | TINYINT | 默认1 | 1试用 / 2在职 / 3离职 |
| education | VARCHAR(20) | | 学历 |
| bank_card | VARCHAR(30) | | 工资卡号 |
| address | VARCHAR(255) | | 住址 |

### 3.9 考勤记录表 `hr_attendance`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| employee_id | BIGINT | 联合唯一 | 员工ID |
| emp_name | VARCHAR(50) | | 姓名快照 |
| dept_id | BIGINT | | 部门快照 |
| attend_date | DATE | 联合唯一 | 考勤日期 |
| clock_in | TIME | | 上班打卡 |
| clock_out | TIME | | 下班打卡 |
| status | VARCHAR(20) | 默认NORMAL | NORMAL/LATE/EARLY/ABSENT/LEAVE/OVERTIME |
| leave_type | TINYINT | | 1事假 / 2病假 / 3年假 |
| overtime_hours | DECIMAL(5,2) | 默认0 | 加班小时数 |
| remark | VARCHAR(255) | | 备注 |

### 3.10 薪资档案表 `hr_salary_standard`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| employee_id | BIGINT | 唯一 | 员工ID |
| base_salary | DECIMAL(10,2) | | 基本工资 |
| post_salary | DECIMAL(10,2) | | 岗位工资 |
| perf_salary | DECIMAL(10,2) | | 绩效工资 |
| social_base | DECIMAL(10,2) | | 社保缴纳基数 |
| fund_base | DECIMAL(10,2) | | 公积金缴纳基数 |
| bank_card | VARCHAR(30) | | 工资卡号 |

### 3.11 工资单表 `hr_payroll`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| employee_id | BIGINT | 联合唯一 | 员工ID |
| salary_month | CHAR(7) | 联合唯一 | 发薪月份（如 2026-09） |
| emp_name / dept_name / post_name | VARCHAR | | 快照字段 |
| base_salary / post_salary / perf_salary | DECIMAL(10,2) | | 各项工资 |
| overtime_pay | DECIMAL(10,2) | | 加班费 |
| attendance_deduct | DECIMAL(10,2) | | 考勤扣款 |
| gross_pay | DECIMAL(10,2) | | 应发工资 |
| social_personal / fund_personal | DECIMAL(10,2) | | 社保/公积金个人部分 |
| tax | DECIMAL(10,2) | | 个人所得税 |
| net_pay | DECIMAL(10,2) | | 实发工资 |
| remark | VARCHAR(255) | | 备注 |

### 3.12 登录日志表 `sys_login_log`
| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK | 主键 |
| username | VARCHAR(50) | | 登录账号 |
| ip | VARCHAR(64) | | 登录IP |
| login_time | DATETIME | | 登录时间 |
| status | TINYINT | 默认1 | 1成功 / 0失败 |
| msg | VARCHAR(255) | | 结果说明 |

---

## 4. 索引设计

| 表 | 索引 | 类型 | 作用 |
|---|---|---|---|
| sys_user | uk_username | 唯一 | 账号唯一 |
| sys_user | idx_employee / idx_status | 普通 | 员工关联、状态过滤 |
| sys_role | uk_role_key | 唯一 | 角色标识唯一 |
| hr_employee | uk_emp_code | 唯一 | 工号唯一 |
| hr_employee | idx_dept / idx_name / idx_status | 普通 | 部门、姓名、状态检索 |
| hr_attendance | uk_emp_date | 唯一 | 防重复考勤 |
| hr_attendance | idx_date / idx_dept | 普通 | 日期、部门统计 |
| hr_salary_standard | uk_emp | 唯一 | 每员工一份档案 |
| hr_payroll | uk_emp_month | 唯一 | 防重复工资单 |
| hr_payroll | idx_month / idx_dept_name | 普通 | 月份、部门统计 |

---

## 5. ER 关系概览

```
sys_user ──< sys_user_role >── sys_role ──< sys_role_menu >── sys_menu
   │ (employee_id)
   ▼
hr_employee ──< hr_attendance
   │ ──< hr_salary_standard
   │ ──< hr_payroll
   │ (dept_id)──> hr_department (parent_id 自关联树)
   │ (post_id)──> hr_position
hr_department (leader_id) ──> hr_employee
```

- **RBAC 关系**：用户↔角色↔菜单 通过两张关联表多对多。
- **业务关系**：员工为人事主表，考勤/薪资档案/工资单均以 `employee_id` 关联员工；部门/岗位以逻辑外键关联。

---

## 6. 初始数据与权限模型

### 6.1 内置角色
| role_key | role_name | 权限范围 |
|---|---|---|
| admin | 系统管理员 | 全部权限 |
| hr | 人事专员 | 组织/员工/考勤/薪酬业务（不含系统配置） |
| employee | 普通员工 | 仅数据看板与个人中心（行级权限） |

### 6.2 权限分配原则
- 管理员授予 `sys_menu` 全部节点。
- 人事专员授予除"系统管理"目录（用户/角色/菜单）以外的所有业务节点。
- 普通员工仅授予看板与业务查看类权限；其个人数据访问由接口层 `empId` 行级过滤保障，不依赖菜单授权。

### 6.3 安全约束
- 普通员工角色不可通过 `/api/employee/{id}`、`/api/attendance/{id}`、`/api/salary/standard/{employeeId}`、`/api/salary/payroll/{id}` 等详情接口访问他人数据；接口层对 `employee` 角色做归属校验，越权返回业务异常。
- 密码字段仅存储 BCrypt 哈希，初始化由后端写入以保证算法一致。

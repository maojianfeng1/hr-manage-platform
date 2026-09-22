# HR 管理系统

基于 **Spring Boot 3 + Vue 3** 的人力资源管理系统，覆盖组织、员工、考勤、薪酬等核心人事业务，并基于 **RBAC 模型**实现细粒度的功能权限与行级数据权限控制。

## 技术栈

### 后端
- Java 21 / Spring Boot 3.2
- MyBatis + MySQL 8.0
- JWT（无状态鉴权，HS256）+ BCrypt（密码加密）；登录态由客户端 LocalStorage 维护，后端以 ThreadLocal 上下文处理，无需 Redis 等缓存中间件
- 统一返回结果、全局异常处理、自定义权限注解

### 前端
- Vue 3 + Vite
- Element Plus + Axios
- Pinia（状态管理）+ Vue Router
- ECharts（数据可视化看板）

## 功能模块

| 模块 | 说明 |
|------|------|
| 系统管理 | 用户管理、角色管理、菜单管理（RBAC 三件套） |
| 组织管理 | 部门管理、岗位管理 |
| 员工管理 | 员工花名册维护 |
| 考勤管理 / 薪酬管理 | 考勤记录、薪资核算 |
| 数据看板 | 企业全员看板（管理员/HR） + 个人看板（员工，行级数据权限） |

### 权限设计
- **功能权限（按钮级）**：基于 `@RequiresPermission` 注解 + 拦截器，无权限的菜单/按钮前端不渲染、不发起请求。
- **数据权限（行级）**：通过 `UserContext.empId` 实现「普通员工只能查看本人」的数据隔离。

## 环境要求
- JDK 21
- MySQL 8.0（库名 `hr_manage`）
- Node.js 18+

## 快速启动

### 1. 数据库与中间件
```bash
# 创建库并执行建表脚本
mysql -uroot -p hr_manage < docs/02_数据库建表脚本.sql
```

### 2. 后端
- 修改 `backend/src/main/resources/application-dev.yml` 中的数据库连接（或使用环境变量 `DB_PASSWORD`，默认 `123456`）。
- 直接运行 `HrManageApplication`（`server.port=8080`）。
- ⚠️ 注意：若启动环境注入了 `SERVER__PORT` 变量，需先 `unset SERVER__PORT` 或显式指定 `-Dserver.port=8080`，避免端口被劫持为随机端口。

### 3. 前端
```bash
cd frontend
npm install
npm run dev
```
访问 `http://localhost:5173`，前端通过代理 `/api` 转发到后端 8080。

## 默认演示账号

| 账号 | 密码 | 角色 | 权限范围 |
|------|------|------|----------|
| admin | 123456 | 系统管理员 | 全部功能，含系统配置 |
| hr | 123456 | 人事专员 | 业务模块，不含系统配置 |
| employee | 123456 | 普通员工 | 仅个人看板（行级数据权限） |

> 首次启动由 `DataInitializer` 自动创建上述三个账号（密码统一 `123456`，BCrypt 加密入库）。

## 部署
- **容器化部署（Docker）**：见 [docs/DOCKER_容器化部署手册.md](docs/DOCKER_容器化部署手册.md)（mysql + backend + frontend 三服务编排）。
- **部署与运维说明**：见 [docs/DEPLOY_部署与运维手册.md](docs/DEPLOY_部署与运维手册.md)。

## 目录结构
```
hr-manage-platform/
├── backend/        # Spring Boot 后端（Java 21）
│   └── src/main/   # 实体 / Mapper / Service / Controller / 配置
├── frontend/       # Vue 3 前端（Vite + Element Plus）
│   └── src/        # views / api / router / stores / layout
└── docs/           # 需求、建库脚本、模块手册、部署手册
```

## 开发文档
- `docs/SRS_需求规格说明书.md` — 需求规格说明书
- `docs/API_接口设计文档.md` — 接口设计文档
- `docs/DB_数据库设计说明书.md` — 数据库设计说明书
- `docs/TEST_测试用例文档.md` — 测试用例文档
- `docs/DEPLOY_部署与运维手册.md` — 部署与运维手册
- `docs/DOCKER_容器化部署手册.md` — 容器化部署手册
- `docs/02_数据库建表脚本.sql` — 数据库初始化脚本

# 人力资源管理服务平台 部署与运维手册

> 版本：V1.0  
> 文档类型：部署与运维手册（Deployment & Operations Manual）  
> 适用对象：运维工程师、实施人员、开发与测试人员

---

## 1. 引言

### 1.1 编写目的
本文档规定"人力资源管理服务平台"（项目代号：`hr-manage-platform`）的部署架构、环境准备、本地与生产部署流程、部署验证标准及日常运维操作，作为系统上线与运维的统一依据。

### 1.2 适用范围
适用于开发联调、测试环境搭建、生产环境发布及后续运维巡检。详细的接口测试与功能用例见《TEST_测试用例文档》。

### 1.3 术语与缩略语

| 术语 | 说明 |
|---|---|
| 反向代理 | 由 Nginx 统一接收外部请求并转发至后端服务，隐藏后端地址、规避跨域 |
| 逻辑删除 | 以 `is_deleted` 标记记录失效，不物理删除数据 |
| 健康检查 | 后端 `/api/health` 接口，用于探测服务可用性 |

---

## 2. 部署架构概述

系统采用前后端分离架构：

- 前端：Vue 3 静态资源，经 Nginx 托管并提供 `/api` 反向代理；
- 后端：Spring Boot 应用，默认监听 `8080`；
- 数据库：MySQL 8.0.34，库名 `hr_manage`。

开发期由 Vite 代理转发 `/api`；生产期由 Nginx 反向代理，用户仅需访问统一入口（默认 80 端口），前后端均由 Nginx 收口。

---

## 3. 环境准备

### 3.1 依赖组件

| 组件 | 版本要求 | 说明 |
|---|---|---|
| JDK | 21 | 后端运行环境 |
| MySQL | 8.0.34 | 数据库，库名 `hr_manage` |
| Node.js | 18+ | 前端构建环境 |
| Maven | 3.9+ | 后端构建（可选，可用 IDE 内置） |
| Nginx | 任意稳定版 | 生产环境反向代理（可选） |

> **鉴权说明**：系统采用 JWT（HS256）无状态令牌，登录态保存在客户端 LocalStorage，后端以 ThreadLocal 维护用户上下文，**不依赖 Redis 等外部缓存中间件**，部署时无需单独准备缓存服务。

### 3.2 数据库初始化
执行建库与初始化脚本：
```bash
mysql -uroot -p hr_manage < docs/02_数据库建表脚本.sql
```
脚本包含建表、菜单初始化与三套演示账号（admin / hr / employee，密码均为 `123456`，BCrypt 加密）。`DataInitializer` 仅在库为空时创建演示账号，重复执行不会覆盖已有数据。

### 3.3 注意事项
- **环境变量冲突**：若启动环境已注入 `SERVER__PORT` 变量，Spring Boot 宽松绑定会将其识别为 `server.port=0`，导致后端监听随机端口、前端代理失败。建议在 IDE 运行配置的 Environment variables 中显式设置 `SERVER__PORT=8080`，或在命令行启动前执行 `unset SERVER__PORT`。
- **前端命令目录**：`package.json` 位于 `frontend/` 子目录，须在 `frontend/` 下执行 `npm install` 与 `npm run dev`，在根目录执行会报 `ENOENT`。
- **端口占用**：后端默认 `8080`、前端默认 `5173`，启动前确认端口未被占用。

---

## 4. 本地开发环境部署

### 4.1 启动后端

**方式一：IDE（推荐）**
1. 以 `backend/` 为工程根目录打开项目；
2. 运行主类 `HrManageApplication`；
3. 在 Run/Debug Configuration 的 Environment variables 中处理 3.3 所述端口变量；
4. 启动后访问 `http://localhost:8080/api/health`，返回 `{"code":200,...}` 即成功。

**方式二：Maven 命令行**
```bash
cd /e/java-web/hr-manage-platform/backend
mvn clean spring-boot:run
```
> 若本地 Maven 需指定独立仓库或 JDK，请按本机环境补充 `-s` / `-Dmaven.repo.local` 等参数。

如后端启动报 `Address already in use :8080`，先定位占用进程并终止：
```bash
netstat -ano | findstr :8080
taskkill /PID <pid> /F
```

### 4.2 启动前端
```bash
cd E:\java-web\hr-manage-platform\frontend
npm install
npm run dev
```
终端输出 `VITE ready` 后，浏览器访问 `http://localhost:5173` 进入登录页（端口被占用时会自动顺延，以终端实际输出为准）。

---

## 5. 生产环境部署

### 5.1 后端打包
```bash
cd /e/java-web/hr-manage-platform/backend
mvn clean package -DskipTests
```
产物：`backend/target/hr-manage-backend-1.0.0.jar`。运行时建议通过 `application-prod.yml` 覆盖端口、数据库连接并关闭开发期调试端点：
```bash
java -jar backend/target/hr-manage-backend-1.0.0.jar --spring.profiles.active=prod
```

### 5.2 前端打包
```bash
cd E:\java-web\hr-manage-platform\frontend
npm install
npm run build
```
产物：`frontend/dist/`（纯静态资源）。

### 5.3 Nginx 反向代理
```nginx
server {
    listen 80;
    server_name hr.example.com;   # 改为实际域名或 localhost

    location / {
        root  /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;   # 支持 Vue Router history 模式
    }

    location /api {
        proxy_pass http://127.0.0.1:8080;   # 后端 jar 实际地址
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```
统一入口后，浏览器不再存在跨域问题，前后端均由 Nginx 收口。

### 5.4 生产安全建议
- 数据库密码、JWT 密钥写入 `application-prod.yml` 或通过环境变量注入，**禁止硬编码于代码**；
- 视情况关闭开发期健康检查与调试端点的外部暴露；
- `DataInitializer` 仅在库为空时初始化演示数据，重复部署不会覆盖。

---

## 6. 部署验证检查表
系统上线或环境迁移后，应按下表逐项验证（详细功能与异常用例见《TEST_测试用例文档》）：

- [ ] 后端 `/api/health` 返回 200 且数据库连接正常；
- [ ] 前端登录页正常加载，登录请求经代理转发（`/api/auth/login`）；
- [ ] 三套账号（admin / hr / employee）均可登录，菜单按角色正确渲染；
- [ ] 三角色权限差异符合预期：hr 无系统管理菜单，employee 仅见个人看板；
- [ ] 各业务模块（部门/岗位/员工/考勤/薪酬）增删改查正常；
- [ ] 个人看板（employee）仅展示本人数据，无越权访问；
- [ ] 退出登录后正确跳回登录页；
- [ ] 路由守卫对无权限页面正确拦截。

---

## 7. 运维操作

| 操作 | 命令 / 说明 |
|---|---|
| 启动后端 | `java -jar backend/target/hr-manage-backend-1.0.0.jar` |
| 停止后端 | 终止对应 Java 进程 |
| 查看后端日志 | 标准输出或日志文件（按 `logback-spring.xml` 配置） |
| 健康检查 | `GET /api/health` |
| 前端发布 | 将 `frontend/dist/` 同步至 Nginx 根目录并 reload |

---

## 8. 常见问题排查

| 现象 | 原因 | 处理 |
|------|------|------|
| 后端监听随机端口 / 前端代理 404 | `SERVER__PORT` 环境变量劫持 | 按 3.3 显式设置 `SERVER__PORT=8080` 或 `unset` |
| `npm run dev` 报 ENOENT / package.json 不存在 | 在根目录执行 | 进入 `frontend/` 子目录后执行 |
| 登录后菜单空白 / 一直转圈 | 后端 8080 未启动或代理未生效 | 检查 `/api/health`；确认请求经代理转发 |
| 个人看板空白、数据全 0 | employee 账号未关联员工档案或本人无数据 | 确认 `employee_id` 已关联，个人接口返回空属正常 |
| 红色「没有操作权限」提示 | 调用了无权限的公司级接口 | 后端正常拦截；收回对应菜单授权或调整请求 |
| 端口占用 Address already in use :8080 | 旧进程未关闭 | `netstat -ano | findstr :8080` 定位并 `taskkill /PID <pid> /F` |
| MySQL 连接失败 / 库不存在 | 服务未启动或连错库 | 确认连接 `hr_manage`（非 hrm_db），MySQL 服务在运行 |

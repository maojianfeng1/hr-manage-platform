# 人力资源管理服务平台 容器化部署手册

> 版本：V1.0  
> 文档类型：容器化部署手册（Containerization Deployment Manual）  
> 适用对象：运维工程师、实施人员、开发人员

---

## 1. 引言

### 1.1 编写目的
本文档说明基于 Docker 的容器化部署方案，通过 `docker-compose` 编排 MySQL、后端（Spring Boot）与前端（Nginx）三类服务，实现"镜像即环境"的标准化交付，便于环境隔离、快速复现与演示部署。

### 1.2 适用范围
适用于需要快速交付、环境隔离或演示部署的场景。本地非容器化部署见《DEPLOY_部署与运维手册》。

---

## 2. 容器化架构

```
浏览器
  │  http://localhost:8081  (前端静态页 + /api 反代)
  ▼
[frontend]  nginx:alpine        托管 dist/
  │  /api/*  →  proxy_pass http://backend:8080
  ▼
[backend]   openjdk:21-jdk-slim  运行 hr-manage-backend-*.jar
  │  JDBC  →  jdbc:mysql://mysql:3306/hr_manage
  ▼
[mysql]     mysql:8.0.34         数据卷持久化 hr_manage 库
```

三个容器位于同一 compose 网络，彼此以**服务名**（mysql / backend / frontend）互通，无需使用宿主机 localhost。

---

## 3. 前置条件
1. 安装 Docker Desktop 并确认 `docker -v`、`docker compose version` 可正常输出版本；
2. 构建后端 jar（见《DEPLOY_部署与运维手册》5.1），产物 `backend/target/hr-manage-backend-1.0.0.jar`；
3. 构建前端静态资源（见《DEPLOY_部署与运维手册》5.2），产物 `frontend/dist/`。

---

## 4. 镜像构建

### 4.1 后端 Dockerfile（`backend/Dockerfile`）
```dockerfile
# 基础镜像：Java 21 运行环境（slim 体积小）
FROM openjdk:21-jdk-slim

WORKDIR /app

# 把本地打好的 jar 拷进镜像（通配避免写死版本号）
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 4.2 前端 Dockerfile（`frontend/Dockerfile`）
```dockerfile
# 阶段一：用 node 构建
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 阶段二：用 nginx 托管构建出的静态文件
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 4.3 前端 Nginx 配置（`frontend/nginx.conf`）
```nginx
server {
    listen 80;
    server_name localhost;

    root /usr/share/nginx/html;
    index index.html;

    # SPA 路由兜底：刷新子路由不 404
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端接口反代：前端 axios baseURL 仍为 /api，由 nginx 转到 backend 容器
    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 5. 服务编排（docker-compose.yml）
项目根目录 `docker-compose.yml`：

```yaml
services:
  mysql:
    image: mysql:8.0.34
    container_name: hr-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: "123456"
      MYSQL_DATABASE: "hr_manage"
      TZ: "Asia/Shanghai"
    ports:
      - "3306:3306"   # 可选暴露，便于用 IDEA/Navicat 连接容器内的库
    volumes:
      - mysql-data:/var/lib/mysql
      # 建表+初始化脚本挂入，容器首次启动自动执行（建库表+菜单+admin）
      - ./docs/02_数据库建表脚本.sql:/docker-entrypoint-initdb.d/01-init.sql:ro
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-p123456"]
      interval: 10s
      timeout: 5s
      retries: 10

  backend:
    build: ./backend
    container_name: hr-backend
    restart: always
    ports:
      - "8080:8080"
    environment:
      # 覆盖 application-dev.yml 里的 localhost:3306，改用容器服务名 mysql
      SPRING_DATASOURCE_URL: "jdbc:mysql://mysql:3306/hr_manage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
      SPRING_DATASOURCE_USERNAME: "root"
      SPRING_DATASOURCE_PASSWORD: "123456"
      SERVER_PORT: "8080"
    depends_on:
      mysql:
        condition: service_healthy   # 等 MySQL 真正就绪再起后端

  frontend:
    build: ./frontend
    container_name: hr-frontend
    restart: always
    ports:
      - "8081:80"   # 浏览器访问 http://localhost:8081
    depends_on:
      - backend

volumes:
  mysql-data:
```

> 容器 MySQL 为全新空库，首次启动通过挂载的 `docs/02` 脚本自动建表并初始化菜单与 admin 等数据。该脚本仅在容器首次创建时执行一次。若需复用宿主机已有 `hr_manage` 数据，见第 8 节备选方案。

---

## 6. 配置外置与构建忽略

### 6.1 数据源外置
后端数据源地址通过 compose 环境变量 `SPRING_DATASOURCE_URL` 注入，镜像本身不写死环境，符合配置外置（12-Factor）原则。

### 6.2 .dockerignore
`backend/.dockerignore`：
```
target
.git
.idea
*.iml
```
`frontend/.dockerignore`：
```
node_modules
dist
.git
```

---

## 7. 构建与启动
在项目根目录执行：
```bash
docker compose up -d --build

# 查看后端日志（尤其确认 backend 已连上 mysql）
docker compose logs -f backend

# 查看三容器状态
docker compose ps
```
启动成功后：
- 浏览器访问 **http://localhost:8081** → 登录页（admin / 123456）；
- 后端健康检查 `http://localhost:8080/api/health` 应返回数据库连接正常。

常用运维命令：
```bash
docker compose ps                 # 查看三容器状态
docker compose restart backend    # 仅重启后端
docker compose down               # 停止并删除容器（数据卷 mysql-data 保留）
docker compose down -v            # 连数据卷一并删除（清库，慎用）
docker compose logs -f frontend   # 查看前端 nginx 日志
```

---

## 8. 备选方案：连接宿主机 MySQL
若宿主机 MySQL 已在运行且 `hr_manage` 数据完整，可省略 MySQL 容器，由 backend 连接宿主机。将 compose 精简为 backend + frontend 两服务，并在 backend 环境变量中将数据源指向宿主机：
```yaml
SPRING_DATASOURCE_URL: "jdbc:mysql://host.docker.internal:3306/hr_manage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
```
> Windows / macOS 的 Docker Desktop 原生支持 `host.docker.internal`；Linux 需追加 `extra_hosts: ["host.docker.internal:host-gateway"]`。

---

## 9. 故障排查

| 现象 | 原因 | 处理 |
|------|------|------|
| backend 启动失败，报 CommunicationsException | 无法连接 `mysql:3306` | 确认服务名为 `mysql`；`depends_on` 健康条件生效前稍候重试 |
| 前端可打开但登录网络错误 | nginx 反代未通 | 检查 `proxy_pass http://backend:8080/` 与服务名一致 |
| 容器 MySQL 空库、登录失败 | 全新库未初始化 | 确认 `docs/02` 已挂载至 `/docker-entrypoint-initdb.d/`；脚本仅首次执行 |
| 后端端口被本地实例占用 | 宿主机 IDEA 后端仍运行 | 先停止本地后端，或改映射 `"8085:8080"` |
| 镜像构建卡在 npm/maven 下载 | 容器内无本地依赖缓存 | 先本地构建出 jar/dist 再 COPY；或为 maven 挂载本地仓库卷（进阶） |

---

## 10. 方案设计要点
- **多阶段构建**：前端采用 `node:20-alpine → nginx:alpine` 多阶段构建，仅将 `dist/` 产物拷入运行镜像，显著减小体积；
- **启动顺序保障**：通过 `depends_on: condition: service_healthy` 确保 MySQL 就绪后再启动后端，规避启动竞态；
- **配置外置**：数据源等环境相关参数在 compose 层注入，镜像保持环境无关；
- **持续集成延伸**：CI 中可进一步采用 `maven:3.9 → openjdk:21` 多阶段构建自动产出镜像，推送镜像仓库后由编排系统（如 Kubernetes）调度。

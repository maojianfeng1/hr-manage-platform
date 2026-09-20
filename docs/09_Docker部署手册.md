# 09 · Docker 部署手册（自抄版）

> 适用对象：已跑通前后端、想用 Docker 容器化部署的 you。
> 目标：用 `docker-compose` 一键起 **MySQL + 后端(Spring Boot) + 前端(Nginx)** 三件套。
> 风格：和前面 docs 一致——每个要新建的文件都给完整代码，零省略。

---

## 1. 为什么用 Docker（一句话）
把你这套「Spring Boot + Vue + MySQL」从「依赖本机环境」变成「镜像即环境」，换机器、交付、面试演示都只差一条 `docker-compose up`。

## 2. 部署架构（文字图）
```
浏览器
  │  http://localhost:8081  (前端静态页 + /api 反代)
  ▼
[frontend 容器]  nginx:alpine  托管 dist/
  │  /api/*  →  proxy_pass http://backend:8080
  ▼
[backend 容器]   openjdk:21-jdk-slim  跑 hr-manage-server-*.jar
  │  JDBC  →  jdbc:mysql://mysql:3306/hr_manage
  ▼
[mysql 容器]    mysql:8.0.34   数据卷持久化 hr_manage 库
```
> 三个容器在同一个 compose 网络里，互相用**服务名**（mysql / backend / frontend）访问，不用 localhost。

## 3. 前置准备
1. 安装 Docker Desktop（你本机安装包已在 `D:\develop\Docker\installer\DockerDesktopInstaller.exe`，双击装；若启动报错用 `D:\develop\Docker\scripts\RUN-AS-ADMIN.bat` 右键管理员跑一键修复）。
2. 装好后终端执行 `docker -v` 和 `docker compose version`，能出版本号即 OK。
3. **打后端 jar**（在 `backend/` 目录，用你本地 Maven）：
   ```powershell
   cd E:\java-web\hr-manage-platform\backend
   # 用你之前配好的 Git Bash 命令起 Maven（不要直接敲 mvn）
   "D:/jdk/bin/java" -classpath "D:/develop/apache-maven/apache-maven-3.9.4/boot/plexus-classworlds-2.7.0.jar" -Dclassworlds.conf="D:/develop/apache-maven/apache-maven-3.9.4/bin/m2.conf" -Dmaven.home="D:/develop/apache-maven/apache-maven-3.9.4" -Dmaven.multiModuleProjectDirectory="E:/java-web/hr-manage-platform/backend" org.codehaus.plexus.classworlds.launcher.Launcher -s "D:/develop/apache-maven/apache-maven-3.9.4/conf/settings.xml" -Dmaven.repo.local="D:/develop/apache-maven/apache-maven-3.9.4/mvn_repo" -f "E:/java-web/hr-manage-platform/backend/pom.xml" clean package -DskipTests
   ```
   成功后 `backend/target/` 下出现 `hr-manage-server-1.0.0.jar`。
4. **打前端 dist**（在 `frontend/` 目录）：
   ```powershell
   cd E:\java-web\hr-manage-platform\frontend
   npm install   # 首次或依赖变更时
   npm run build # 产出 frontend/dist/
   ```

---

## 4. 后端 Dockerfile
**新建文件**：`backend/Dockerfile`（注意没有后缀）

```dockerfile
# 基础镜像：Java 21 运行环境（slim 体积小）
FROM openjdk:21-jdk-slim

# 工作目录
WORKDIR /app

# 把本地打好的 jar 拷进镜像（名字用通配，避免写死版本号）
COPY target/*.jar app.jar

# 暴露后端端口（与 application 里 server.port 一致）
EXPOSE 8080

# 启动命令；SPRING_DATASOURCE_URL 等由 compose 注入，覆盖 yml 里的 localhost
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 5. 前端 Dockerfile + nginx 配置
**新建文件 1**：`frontend/Dockerfile`
```dockerfile
# 阶段一：用 node 构建（你本地是 node，但容器内独立）
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 阶段二：用 nginx 托管构建出的静态文件
FROM nginx:alpine
# 覆盖默认配置，加入 /api 反代
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**新建文件 2**：`frontend/nginx.conf`
```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态资源
    root /usr/share/nginx/html;
    index index.html;

    # SPA 路由兜底：刷新子路由不 404
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端接口反代：前端 axios baseURL 仍是 /api，由 nginx 转到 backend 容器
    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 6. docker-compose.yml（编排三件套）
**新建文件**：项目根目录 `docker-compose.yml`（与 `backend/`、`frontend/` 同级）

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
      - "3306:3306"   # 可选暴露，方便你用 IDEA/Navicat 连容器里的库
    volumes:
      - mysql-data:/var/lib/mysql
      # 把建表+初始化脚本挂进去，容器首次启动自动执行（建库表+菜单+admin）
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

> **关于初始化脚本的坑**：容器 MySQL 是**全新空库**，靠 `docs/02` 脚本自动建表+插入菜单/admin 等初始数据。如果你本机 `hr_manage` 已有数据、想**直接复用**，看第 9 节的「连本机 MySQL」方案，别用这个 MySQL 容器。

---

## 7. .dockerignore（避免把垃圾打进镜像）
**新建文件**：`backend/.dockerignore`
```
target
.git
.idea
*.iml
```
**新建文件**：`frontend/.dockerignore`
```
node_modules
dist
.git
```

---

## 8. 构建与启动命令（在**项目根目录**执行）
```powershell
cd E:\java-web\hr-manage-platform

# 首次/改动后重新构建镜像并启动（-d 后台运行）
docker compose up -d --build

# 看启动日志（尤其 backend 是否连上 mysql）
docker compose logs -f backend

# 只看 mysql 是否 healthy
docker compose ps
```

启动成功后：
- 浏览器开 **http://localhost:8081** → 登录页（admin / 123456）
- 后端接口：http://localhost:8080/api/health 应返回 db connected

**常用运维命令**
```powershell
docker compose ps                 # 看三容器状态
docker compose restart backend    # 只重启后端
docker compose down               # 停并删容器（数据卷 mysql-data 保留）
docker compose down -v            # 连数据卷一起删（清库，慎用）
docker compose logs -f frontend   # 看前端 nginx 日志
```

---

## 9. 备选方案：连你本机已运行的 MySQL（不另起 MySQL 容器）
如果你本机 `D:\develop\mysql-8.0.34` 已在跑、且 `hr_manage` 数据都在，**不想再起一个 MySQL 容器**，把 compose 改成两服务，并让 backend 连宿主 MySQL：

`docker-compose.yml`（精简版）：
```yaml
services:
  backend:
    build: ./backend
    container_name: hr-backend
    restart: always
    ports:
      - "8080:8080"
    environment:
      # Windows/Mac 上 host.docker.internal 指向宿主机
      SPRING_DATASOURCE_URL: "jdbc:mysql://host.docker.internal:3306/hr_manage?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
      SPRING_DATASOURCE_USERNAME: "root"
      SPRING_DATASOURCE_PASSWORD: "123456"
      SERVER_PORT: "8080"

  frontend:
    build: ./frontend
    container_name: hr-frontend
    restart: always
    ports:
      - "8081:80"
    depends_on:
      - backend
```
> 注意：Linux 上 `host.docker.internal` 默认不通，需加 `extra_hosts: ["host.docker.internal:host-gateway"]`。Windows/Mac 的 Docker Desktop 直接可用。

---

## 10. 常见坑速查
| 现象 | 原因 | 解决 |
|------|------|------|
| backend 起不来，日志 `com.mysql.cj.jdbc.exceptions.CommunicationsException` | 连 `mysql:3306` 失败 | 确认 compose 里 mysql 服务名就是 `mysql`；或 `depends_on` 的 healthy 条件没生效，等几秒重试 |
| 前端页面能开，但登录报网络错误 | nginx 反代没通 | 查 `frontend/nginx.conf` 的 `proxy_pass http://backend:8080/`；确认 backend 容器名/服务名是 `backend` |
| 容器 MySQL 库是空的、登录失败 | 全新库未初始化 | 确认 `docs/02` 脚本已挂到 `/docker-entrypoint-initdb.d/`；**该脚本只在容器首次创建时执行一次**，之后改卷不会重跑 |
| 后端端口被之前的本地实例占 | 你本机 IDEA 起的后端还跑着 8080 | 先停掉本地后端再 `docker compose up`，或改 compose 映射 `"8085:8080"` |
| 镜像构建卡在 `npm install` / `maven` 下载 | 容器无本地依赖缓存 | 用第 3 节「先本地 package/build 再 COPY」的简化思路；或给 maven 挂本地仓库卷（进阶） |

---

## 11. 面试谈资（可背）
- 「项目前后端分离，后端 Spring Boot 打 jar 进 `openjdk:21-slim` 镜像，前端 Vite 构建后由 `nginx:alpine` 托管并反代 `/api`，MySQL 独立容器 + 数据卷持久化。」
- 「用 `docker-compose` 编排三服务，通过 `depends_on: condition: service_healthy` 保证 MySQL 就绪后再起后端，避免启动竞态。」
- 「数据源地址用环境变量在 compose 层注入覆盖，镜像本身不写死环境，符合 12-Factor 配置外置原则。」
- 「CI 里可进一步用多阶段 Maven 构建（`maven:3.9 → openjdk:21`）自动出镜像，推私有仓库交给 K8s 调度。」

---

## 12. 你接下来怎么抄
1. 先在 `backend/`、`frontend/` 各建 Dockerfile、在 frontend 建 nginx.conf、根目录建 docker-compose.yml 与两个 .dockerignore；
2. 按第 3 节先本地打出 `target/*.jar` 和 `dist/`；
3. 装好 Docker Desktop 后，根目录 `docker compose up -d --build`；
4. 开 http://localhost:8081 用 admin/123456 验证；
5. 遇错把日志贴给我定位。

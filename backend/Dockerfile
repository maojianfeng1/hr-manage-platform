# 基础镜像：Java 21 运行环境（slim 体积小）
FROM openjdk:21-jdk-slim

# 工作目录
WORKDIR /app

# 把本地打好的 jar 拷进镜像（名字用通配，避免写死版本号）
COPY target/*.jar app.jar

# 暴露后端端口（与 application 里 server.port 一致）
EXPOSE 8080

# 启动命令；数据源地址由 compose 注入 SPRING_DATASOURCE_URL 覆盖 yml 里的 localhost
ENTRYPOINT ["java", "-jar", "app.jar"]

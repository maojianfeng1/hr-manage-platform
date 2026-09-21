# 阶段一：用 node 构建（容器内独立环境）
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

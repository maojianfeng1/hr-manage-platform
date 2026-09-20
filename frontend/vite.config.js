import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

/**
 * Vite 配置
 *
 * 知识点：
 * 1. server.proxy：开发环境把 /api 开头的请求转发到后端 8080，解决跨域。
 *    注意「不要」写 rewrite —— 因为后端接口本身就是 /api/xxx，
 *    写成 rewrite 把 /api 去掉，就会变成请求 /health 而 404。这是新手最常见的坑。
 * 2. resolve.alias：把 @ 映射到 src 目录，import 时不用写 ../../../ 这种相对路径。
 * 3. 为什么前端要配 proxy，后端又配了 CORS？两者选一即可。
 *    proxy 是「开发期」的代理，生产环境由 Nginx 转发；CORS 是后端长期开放的兜底。
 */
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
        // 不要加 rewrite：后端路径本身含 /api
      }
    }
  }
})

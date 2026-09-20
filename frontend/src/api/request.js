import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'

/**
 * Axios 全局封装
 *
 * 知识点（前端面试高频）：
 * 1. 请求拦截器：统一往 header 里塞 token，业务代码不用每处都写。
 * 2. 响应拦截器：后端统一返回 { code, msg, data }，
 *    在这里判断 code，成功就 return res（业务层少写一层 .data），失败就统一弹提示。
 * 3. 401 处理：说明 token 失效，清掉本地 token 并跳登录页。
 *    这里用 window.location.href 而不是 router.push ——
 *    因为 request.js 被 router 间接依赖，直接 import router 会形成循环依赖。
 */
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      // 与后端约定的格式：Authorization: Bearer <token>
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 后端约定：code === 200 才是业务成功
    if (res.code === 200) {
      return res
    }

    if (res.code === 401) {
      ElMessage.error(res.msg || '登录已过期，请重新登录')
      removeToken()
      window.location.href = '/login'
      return Promise.reject(new Error(res.msg || 'Unauthorized'))
    }

    if (res.code === 403) {
      ElMessage.error(res.msg || '没有操作权限')
      return Promise.reject(new Error(res.msg || 'Forbidden'))
    }

    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  (error) => {
    // 网络层错误：后端没启动、跨域被拦、超时等
    const msg = error.response?.status
      ? `请求失败（HTTP ${error.response.status}）`
      : '网络异常，请确认后端服务已启动'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service

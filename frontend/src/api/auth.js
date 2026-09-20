import request from './request'

/**
 * 认证相关接口
 */
export function login(data) {
  // baseURL 是 /api，所以这里写 '/auth/login'，最终请求 /api/auth/login
  return request.post('/auth/login', data)
}

/** 获取当前登录用户信息（刷新页面后用来恢复 Pinia 状态） */
export function getUserInfo() {
  return request.get('/auth/info')
}

/** 退出登录（通知后端作废，实际无状态 token 主要靠前端清本地） */
export function logout() {
  return request.post('/auth/logout')
}

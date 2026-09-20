const TOKEN_KEY = 'hr_token'

/**
 * token 的本地存储
 *
 * 知识点：为什么放 localStorage 而不是 Pinia？
 * Pinia 的数据存在内存里，浏览器一刷新就没了。
 * localStorage 持久化，刷新后还能读出来，所以「存」用 localStorage，「用」从 Pinia 取。
 * 安全提示：localStorage 有 XSS 风险，企业级更推荐 HttpOnly Cookie，
 * 演示项目用 localStorage 是行业常见做法，面试被问到要能说清这个取舍。
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

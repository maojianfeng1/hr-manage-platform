import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login as loginApi, getUserInfo, logout as logoutApi } from '@/api/auth'
import { getMenus } from '@/api/menu'

/**
 * 用户状态仓库
 *
 * 知识点：
 * 1. token、用户信息、角色、权限、菜单这些「多页面共享」的数据放 Pinia 全局共享；
 *    token 额外用 localStorage 持久化（刷新不丢），所以「存」走 localStorage、「用」走 Pinia。
 * 2. Pinia 是内存态，刷新页面会清空 —— 所以刷新后要在 Layout 挂载时调 fetchUserInfo / fetchMenus
 *    重新从后端拉取（JWT 无状态，后端靠 token 辨认当前用户）。
 *
 * roles / perms 的值是后端下发的原始值：
 *   roles 如 ['admin'] / ['hr'] / ['employee']（小写，对应 sys_role.role_key）
 *   perms 如 ['emp:edit', 'sal:payroll:edit']
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null,
    roles: [],
    perms: [],
    menus: []            // 当前用户可见的菜单树（来自 /api/menu/user-menus）
  }),

  getters: {
    isLogin: (state) => !!state.token,
    // 角色展示名映射
    roleName() {
      const map = { admin: '系统管理员', hr: '人事专员', employee: '普通员工' }
      return this.roles.map((r) => map[r] || r).join('、')
    },
    // 按钮级权限判断：v-if="userStore.hasPerm('emp:edit')"
    hasPerm: (state) => (perm) => state.perms.includes(perm)
  },

  actions: {
    async login(form) {
      const res = await loginApi(form)
      const { token, userInfo, roles, perms } = res.data
      this.setUserToken(token)
      this.setUserInfo({ userInfo, roles, perms })
      return res
    },

    setUserToken(token) {
      this.token = token
      setToken(token)
    },

    setUserInfo({ userInfo, roles, perms }) {
      this.userInfo = userInfo
      this.roles = roles || []
      this.perms = perms || []
    },

    setMenus(menus) {
      this.menus = menus || []
    },

    /** 刷新页面后，用 token 重新拉取用户信息（恢复 Pinia 内存态） */
    async fetchUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data
      this.roles = res.data.roles || []
      this.perms = res.data.perms || []
      return res.data
    },

    /** 拉取当前用户可见菜单树 */
    async fetchMenus() {
      const res = await getMenus()
      this.menus = res.data || []
      return res.data
    },

    /**
     * 同步清空本地登录态（不依赖网络）。
     * 用于退出登录：先立刻清掉本地状态并跳登录页，
     * 后端注销请求放后台异步发，失败也不影响前端退出。
     */
    resetAuth() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      this.perms = []
      this.menus = []
      removeToken()
    },

    async logout() {
      // 兼容旧调用：先同步清本地，再后台通知后端作废
      this.resetAuth()
      try {
        await logoutApi()
      } catch (e) {
        // 即便后端调用失败，前端本地状态也已清掉
      }
    }
  }
})

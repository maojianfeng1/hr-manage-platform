<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-badge">HR</div>
        <div class="logo-text">
          <div class="logo-name">HR 管理平台</div>
          <div class="logo-sub">人力资源管理系统</div>
        </div>
      </div>
      <!-- 侧边栏：菜单数据来自后端按角色下发的 menuTree，不再是写死的 -->
      <el-scrollbar>
        <el-menu
          :default-active="$route.path"
          router
          class="menu"
          background-color="#1f2d3d"
          text-color="#a3b1c2"
          active-text-color="#ffffff"
        >
          <SideMenu :menus="userStore.menus" />
        </el-menu>
      </el-scrollbar>
      <div class="aside-footer">v1.0 · 内部系统</div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <!-- 面包屑：显示当前页面标题（来自路由 meta.title） -->
        <div class="crumb">
          <el-icon class="crumb-icon"><Location /></el-icon>
          <span class="crumb-parent">人力资源管理平台</span>
          <template v-if="pageTitle && pageTitle !== '首页看板'">
            <span class="crumb-sep">/</span>
            <span class="crumb-current">{{ pageTitle }}</span>
          </template>
        </div>

        <div class="user-area">
          <el-dropdown>
            <span class="user-trigger">
              <el-avatar :size="30" class="user-avatar">
                {{ avatarText }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.realName || '未登录' }}</span>
              <span class="user-role">{{ userStore.roleName || '' }}</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/auth'
import SideMenu from '@/components/SideMenu.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 面包屑当前页标题（路由 meta.title，见 router/index.js）
const pageTitle = computed(() => route.meta?.title || '')

// 头像显示真实姓名末尾 1~2 个字（中文场景比图标更有辨识度）
const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || ''
  return name ? name.slice(-2) : '?'
})

/**
 * Layout 挂载时恢复「刷新会丢」的内存态：
 * 1. 没有 userInfo（刷新后 Pinia 清空）→ 调 /api/auth/info 重新拉用户
 * 2. 没有 menus → 调 /api/menu/user-menus 拉动态菜单
 * 这两个动作保证「刷新页面后菜单和用户名依然在」。
 */
onMounted(async () => {
  try {
    if (!userStore.userInfo) {
      await userStore.fetchUserInfo()
    }
    if (!userStore.menus || userStore.menus.length === 0) {
      await userStore.fetchMenus()
    }
  } catch (e) {
    // 拉取失败（如 token 失效）由响应拦截器统一处理跳登录
  }
})

/**
 * 退出登录。
 * 用下拉项的原生 @click 触发（比 el-dropdown 的 @command 更可靠，
 * 后者首次点击常被弹出层的"点击外部关闭"逻辑吞掉，导致要点两次）。
 * 逻辑：先同步清本地状态并跳登录页，后端注销请求放后台异步发，不影响前端退出。
 */
async function handleLogout() {
  userStore.resetAuth()           // 立刻清 token + 用户信息 + 权限 + 菜单
  router.push('/login')           // 此时守卫读到 token 已空，直接放行到登录页
  ElMessage.success('已退出登录')
  // 后台通知服务端作废 token；失败也不影响前端（无状态 token 主要靠前端清本地）
  logoutApi().catch(() => {})
}
</script>

<style scoped>
.layout { height: 100vh; }

/* ---------- 侧边栏：深色企业风 ---------- */
.aside {
  background: #1f2d3d;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}
.logo-badge {
  width: 34px; height: 34px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3a8ee6, #2b6cb8);
  color: #fff;
  font-size: 14px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}
.logo-name { font-size: 15px; font-weight: 600; color: #ffffff; line-height: 1.3; }
.logo-sub { font-size: 11px; color: #7a8ba0; line-height: 1.3; }

.menu { border-right: none; }
/* 菜单激活项：左侧主色条 + 浅色底，业务系统常见样式 */
.menu :deep(.el-menu-item.is-active) {
  background: #2b6cb8 !important;
  color: #ffffff !important;
}
.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
}
.aside-footer {
  flex-shrink: 0;
  padding: 12px 16px;
  font-size: 11px;
  color: #5f7189;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

/* ---------- 顶栏 ---------- */
.header {
  display: flex; align-items: center; justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 2px rgba(15, 30, 50, 0.03);
  z-index: 5;
}
.crumb { display: flex; align-items: center; gap: 6px; font-size: 14px; }
.crumb-icon { color: #98a6bd; }
.crumb-parent { color: #8f9bb3; }
.crumb-sep { color: #c0c8d4; margin: 0 2px; }
.crumb-current { color: #1f2d3d; font-weight: 600; }

.user-area { cursor: pointer; }
.user-trigger { display: flex; align-items: center; gap: 8px; color: #1f2d3d; outline: none; }
.user-avatar {
  background: #2b6cb8;
  color: #fff;
  font-size: 12px;
}
.user-name { font-size: 14px; font-weight: 500; }
.user-role {
  font-size: 12px;
  color: #2b6cb8;
  background: #eaf2fc;
  border-radius: 4px;
  padding: 1px 8px;
}
.arrow { color: #98a6bd; font-size: 12px; }

.main { background: #f0f2f5; padding: 16px; }
</style>

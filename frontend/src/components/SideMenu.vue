<template>
  <template v-for="item in visibleMenus" :key="item.id">
    <!-- 有子菜单：渲染为可展开的 el-sub-menu（递归自身） -->
    <el-sub-menu v-if="item.children && item.children.length" :index="item.path || String(item.id)">
      <template #title>
        <el-icon v-if="item.icon"><component :is="resolveIcon(item.icon)" /></el-icon>
        <span>{{ item.menuName }}</span>
      </template>
      <!-- 递归渲染子级 -->
      <SideMenu :menus="item.children" />
    </el-sub-menu>

    <!-- 叶子菜单：可点击导航 -->
    <el-menu-item v-else :index="item.path || String(item.id)">
      <el-icon v-if="item.icon"><component :is="resolveIcon(item.icon)" /></el-icon>
      <template #title>{{ item.menuName }}</template>
    </el-menu-item>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 递归组件必须显式命名，模板里才能 <SideMenu> 引用自身
defineOptions({ name: 'SideMenu' })

const props = defineProps({
  menus: { type: Array, default: () => [] }
})

const router = useRouter()
const userStore = useUserStore()

/**
 * 路由 path（如 /employee、/org/dept）→ 该模块需要的「查看」权限点。
 * 取自 router 各路由的 meta.permission，前端菜单据此决定显示与否，
 * 与后端 @RequiresPermission 用的是同一套权限码，前后端一致。
 */
const permByPath = {}
router.getRoutes().forEach((r) => {
  if (r.meta && r.meta.permission) permByPath[r.path] = r.meta.permission
})

function permForPath(path) {
  if (!path) return null
  const norm = String(path).replace(/^\//, '') // 去掉前导斜杠，统一成 'employee' / 'org/dept'
  return permByPath[path] || permByPath['/' + norm] || permByPath[norm] || null
}

/**
 * 是否显示某个菜单项：
 * 1. 没配置权限要求的（如首页看板）→ 一律显示
 * 2. perms 还没从后端加载完（刷新瞬间）→ 先显示，避免菜单全闪没；加载完会重新计算
 * 3. 已加载 → 用 hasPerm 判定
 */
function canShow(item) {
  const perm = permForPath(item.path)
  if (!perm) return true
  if (!userStore.perms || userStore.perms.length === 0) return true
  return userStore.hasPerm(perm)
}

/** 递归过滤：隐藏无权限项；若某子菜单下子项全被隐藏，则该子菜单本身也不显示 */
function filterTree(list) {
  if (!list) return []
  const result = []
  for (const item of list) {
    if (!canShow(item)) continue
    if (item.children && item.children.length) {
      const children = filterTree(item.children)
      if (children.length > 0) result.push({ ...item, children })
    } else {
      result.push(item)
    }
  }
  return result
}

const visibleMenus = computed(() => filterTree(props.menus))

/**
 * 把后端菜单里的 icon 名（如 'setting'）映射到 Element Plus 图标组件。
 * 数据库里存的是小写语义名，和 El 的 PascalCase 组件名不一定一致，
 * 所以用一张映射表兜底，找不到就用默认 Menu 图标，避免渲染报错。
 */
const ICON_MAP = {
  setting: 'Setting',
  user: 'User',
  role: 'UserFilled',
  org: 'OfficeBuilding',
  dept: 'Files',
  post: 'Postcard',
  peoples: 'User',
  clock: 'Clock',
  money: 'Money',
  chart: 'DataLine'
}

function resolveIcon(name) {
  const comp = ICON_MAP[name] || 'Menu'
  return ElementPlusIconsVue[comp] || ElementPlusIconsVue.Menu
}
</script>

import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页看板' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', permission: 'system:user:view' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', permission: 'system:role:view' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理', permission: 'system:menu:view' }
      },
      {
        path: 'org/dept',
        name: 'OrgDept',
        component: () => import('@/views/org/Dept.vue'),
        meta: { title: '部门管理', permission: 'org:dept:view' }
      },
      {
        path: 'org/post',
        name: 'OrgPost',
        component: () => import('@/views/org/Post.vue'),
        meta: { title: '岗位管理', permission: 'org:post:view' }
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/views/employee/Employee.vue'),
        meta: { title: '员工管理', permission: 'emp:view' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/Attendance.vue'),
        meta: { title: '考勤管理', permission: 'att:view' }
      },
      {
        path: 'salary',
        name: 'Salary',
        component: () => import('@/views/salary/Salary.vue'),
        meta: { title: '薪酬管理', permission: 'sal:view' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const WHITE_LIST = ['/login']

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 人力资源管理平台` : '人力资源管理服务平台'
  const token = getToken()
  if (token) {
    if (to.path === '/login') {
      next('/')
    } else {
      // 已登录但访问无权限页面：perms 已加载才拦截（避免刷新瞬间误杀），无权限退回看板
      const userStore = useUserStore()
      const needPerm = to.meta && to.meta.permission
      if (needPerm && userStore.perms.length > 0 && !userStore.hasPerm(needPerm)) {
        ElMessage.warning('没有访问该页面的权限')
        next('/dashboard')
      } else {
        next()
      }
    }
  } else {
    if (WHITE_LIST.includes(to.path)) next()
    else next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }
})

export default router

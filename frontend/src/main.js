import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './styles/index.css'

/**
 * 应用入口
 *
 * 知识点：createApp 之后的 use() 是「插件安装」。
 * - Pinia：状态管理，用户信息 / token / 权限都放这里
 * - Router：路由，配合全局前置守卫做登录拦截
 * - ElementPlus：UI 组件库，并指定中文语言包（否则分页器会显示英文）
 * 顺序无所谓，但必须在 mount() 之前。
 */
const app = createApp(App)

// 把 Element Plus 的图标注册成全局组件，模板里可直接 <el-icon><User /></el-icon>
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')

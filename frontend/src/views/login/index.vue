<template>
  <div class="login-page">
    <!-- 左侧品牌区（宽屏显示，窄屏自动隐藏）：体现业务系统定位 -->
    <div class="brand-pane">
      <div class="brand-box">
        <div class="brand-badge">HR</div>
        <h1 class="brand-title">人力资源管理平台</h1>
        <p class="brand-desc">组织 · 员工 · 考勤 · 薪酬 一体化管理</p>
        <ul class="brand-points">
          <li><el-icon><OfficeBuilding /></el-icon> 组织架构与岗位编制统一维护</li>
          <li><el-icon><Clock /></el-icon> 考勤打卡与月度薪酬自动核算</li>
          <li><el-icon><Lock /></el-icon> 基于角色的权限管控（RBAC）</li>
        </ul>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-pane">
      <el-card class="login-card" shadow="never">
        <h2 class="title">用户登录</h2>
        <p class="subtitle">请使用系统账号登录工作台</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <el-alert
          class="demo-tip"
          type="info"
          :closable="false"
          show-icon
          title="演示账号（密码均为 123456）"
        >
          <div>管理员：admin　|　人事：hr　|　普通员工：employee</div>
        </el-alert>
      </el-card>
      <p class="copyright">HR 管理平台 · 仅限内部使用</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

// 校验规则：对应后端 LoginDTO 的 @NotBlank，前端也做一遍（体验更好，减少一次无效请求）
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  // 先让 el-form 跑一遍校验规则
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form)
      const { token, userInfo, roles, perms } = res.data
      // 1. 存 token + 用户信息（token 进 localStorage，用户信息进 Pinia）
      userStore.setUserToken(token)
      userStore.setUserInfo({ userInfo, roles, perms })
      ElMessage.success('登录成功')
      // 2. 跳转到登录前想去的页面，或首页看板
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
}

/* ---------- 左侧品牌区 ---------- */
.brand-pane {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #2b4a6f 60%, #2b6cb8 100%);
  padding: 40px;
}
.brand-box { max-width: 460px; color: #fff; }
.brand-badge {
  width: 48px; height: 48px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.25);
  font-size: 18px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 24px;
}
.brand-title { font-size: 30px; font-weight: 600; margin: 0 0 10px; letter-spacing: 1px; }
.brand-desc { font-size: 15px; color: rgba(255, 255, 255, 0.75); margin: 0 0 32px; }
.brand-points { list-style: none; margin: 0; padding: 0; }
.brand-points li {
  display: flex; align-items: center; gap: 10px;
  font-size: 14px; color: rgba(255, 255, 255, 0.85);
  padding: 8px 0;
}

/* ---------- 右侧表单区 ---------- */
.form-pane {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f7f9fc;
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 380px;
  border: 1px solid #e8edf3;
  border-radius: 10px;
  padding: 8px 6px;
  box-shadow: 0 4px 16px rgba(31, 45, 61, 0.06);
}
.title {
  margin: 8px 0 4px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2d3d;
  text-align: center;
}
.subtitle {
  margin: 0 0 22px;
  font-size: 13px;
  color: #8f9bb3;
  text-align: center;
}
.login-btn {
  width: 100%;
  margin-top: 6px;
}
.demo-tip { margin-top: 16px; }
.copyright { margin-top: 20px; font-size: 12px; color: #a3b1c2; }

/* 窄屏（笔记本小窗）隐藏品牌区，退回居中登录卡 */
@media (max-width: 900px) {
  .brand-pane { display: none; }
  .form-pane { width: 100%; background: linear-gradient(135deg, #1f2d3d 0%, #2b4a6f 60%, #2b6cb8 100%); }
}
</style>

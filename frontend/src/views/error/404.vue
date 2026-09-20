<template>
  <div class="not-found">
    <div class="code">404</div>
    <p class="desc">页面不存在或已被移除</p>
    <el-button type="primary" @click="goHome">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/dashboard')
}
</script>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #f5f7fa;
}

.code {
  font-size: 64px;
  font-weight: 500;
  color: #c0ccda;
  line-height: 1;
}

.desc {
  margin: 0 0 10px;
  font-size: 14px;
  color: #8f9bb3;
}
</style>

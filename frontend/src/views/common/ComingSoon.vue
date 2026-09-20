<template>
  <div class="coming-soon">
    <el-empty description="该模块页面将在阶段⑥开发">
      <el-tag size="large" type="info">{{ title }}</el-tag>
    </el-empty>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title || '功能模块')
</script>

<style scoped>
.coming-soon {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
}
</style>

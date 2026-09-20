<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openAdd(null)">新增顶级菜单</el-button>
    </div>

    <el-table
        :data="tableData"
        row-key="id"
        :tree-props="{ children: 'children' }"
        border
        stripe
        v-loading="loading"
        default-expand-all
    >
      <el-table-column prop="menuName" label="菜单名称" min-width="160" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.menuType === 1" type="primary">菜单</el-tag>
          <el-tag v-else type="warning">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="perms" label="权限标识" min-width="160" />
      <el-table-column prop="path" label="路由路径" min-width="140" />
      <el-table-column prop="component" label="组件路径" min-width="140" />
      <el-table-column prop="icon" label="图标" width="90" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openAdd(row)">新增子项</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
              v-model="form.parentId"
              :data="treeOptions"
              :props="{ label: 'menuName', children: 'children' }"
              value-key="id"
              check-strictly
              default-expand-all
              clearable
              placeholder="选择上级（顶级留空）"
          />
        </el-form-item>
        <el-form-item label="菜单名称" required>
          <el-input v-model="form.menuName" placeholder="如 菜单管理" />
        </el-form-item>
        <el-form-item label="菜单类型" required>
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.perms" placeholder="如 system:menu:edit" />
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.menuType === 1">
          <el-input v-model="form.path" placeholder="如 /system/menu" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 1">
          <el-input v-model="form.component" placeholder="如 system/menu" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="如 menu" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, saveMenu, updateMenu, removeMenu } from '@/api/menu'

const loading = ref(false)
const tableData = ref([])
const treeOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')

const form = reactive({
  id: null, parentId: null, menuName: '', menuType: 1,
  perms: '', path: '', component: '', icon: '', sort: 0
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuList()
    tableData.value = res.data || []
    treeOptions.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, {
    id: null, parentId: null, menuName: '', menuType: 1,
    perms: '', path: '', component: '', icon: '', sort: 0
  })
}

function openAdd(row) {
  dialogTitle.value = row ? `新增「${row.menuName}」的子项` : '新增顶级菜单'
  resetForm()
  if (row) form.parentId = row.id
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId === 0 ? null : row.parentId,
    menuName: row.menuName,
    menuType: row.menuType,
    perms: row.perms,
    path: row.path,
    component: row.component,
    icon: row.icon,
    sort: row.sort
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.menuName) { ElMessage.warning('请填写菜单名称'); return }
  const payload = { ...form }
  if (payload.parentId === null || payload.parentId === undefined) payload.parentId = 0
  const api = form.id ? updateMenu : saveMenu
  await api(payload)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '提示', { type: 'warning' })
  try {
    await removeMenu(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    // 后端返回的错误（如存在子菜单）由响应拦截器统一提示
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 12px; }
</style>

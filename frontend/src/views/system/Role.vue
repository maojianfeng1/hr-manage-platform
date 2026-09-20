<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="success" @click="openAdd">新增角色</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="roleKey" label="角色标识" width="140" />
      <el-table-column prop="roleName" label="角色名称" width="160" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="openAssign(row)">分配权限</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>


    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="角色标识" required>
          <el-input v-model="form.roleKey" :disabled="!!form.id" placeholder="如 hr、audit（英文，程序判断用）" />
        </el-form-item>
        <el-form-item label="角色名称" required>
          <el-input v-model="form.roleName" placeholder="如 人事专员（展示用）" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>


    <el-dialog v-model="assignVisible" title="分配权限（勾选菜单 / 按钮）" width="420px">
      <el-tree
          ref="treeRef"
          :data="menuTreeData"
          :props="{ label: 'menuName', children: 'children' }"
          show-checkbox
          node-key="id"
          :default-checked-keys="checkedMenuIds"
          default-expand-all
      />
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">保存授权</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleList, roleAdd, roleUpdate, roleRemove, roleAssignMenus, menuTree } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const menuTreeData = ref([])
const checkedMenuIds = ref([])
const treeRef = ref(null)

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const form = reactive({ id: null, roleKey: '', roleName: '', remark: '' })

const assignVisible = ref(false)
let assignRoleId = null

async function loadData() {
  loading.value = true
  try {
    const res = await roleList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  const res = await menuTree()
  menuTreeData.value = res.data || []
}

function openAdd() {
  dialogTitle.value = '新增角色'
  Object.assign(form, { id: null, roleKey: '', roleName: '', remark: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑角色'
  Object.assign(form, { id: row.id, roleKey: row.roleKey, roleName: row.roleName, remark: row.remark })
  dialogVisible.value = true
}

async function submit() {
  if (!form.roleKey) { ElMessage.warning('请填写角色标识'); return }
  if (!form.roleName) { ElMessage.warning('请填写角色名称'); return }
  const api = form.id ? roleUpdate : roleAdd
  await api(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function openAssign(row) {
  assignRoleId = row.id
  // 从已加载的角色列表里取该角色的 menuIds（避免重复请求）
  const target = tableData.value.find(r => r.id === row.id)
  checkedMenuIds.value = target && target.menuIds ? [...target.menuIds] : []
  if (menuTreeData.value.length === 0) await loadMenuTree()
  assignVisible.value = true
}

async function submitAssign() {
  // 既取全选的叶子，也取半选的父节点（目录），否则父目录授权会丢
  const checked = treeRef.value.getCheckedKeys()
  const halfChecked = treeRef.value.getHalfCheckedKeys()
  const menuIds = [...checked, ...halfChecked]
  await roleAssignMenus(assignRoleId, menuIds)
  ElMessage.success('授权成功')
  assignVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？该角色下用户的对应权限将一并失效。`, '提示', { type: 'warning' })
  await roleRemove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadMenuTree()
})
</script>

<style scoped>
.page { padding: 16px; }
.toolbar { margin-bottom: 12px; }
</style>

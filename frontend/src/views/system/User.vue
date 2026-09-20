<template>
  <div class="page">

    <el-form :inline="true" :model="query" class="search-bar">
      <el-form-item label="账号">
        <el-input v-model="query.username" placeholder="支持模糊查询" clearable @keyup.enter="loadData" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="query.realName" placeholder="支持模糊查询" clearable @keyup.enter="loadData" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="停用" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openAdd">新增用户</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="登录账号" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">
          <el-tag v-for="rid in row.roleIds || []" :key="rid" style="margin-right: 4px">
            {{ roleNameMap[rid] || rid }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="170">
        <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        @current-change="handlePageChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="登录账号" required>
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="关联员工">
          <el-select v-model="form.employeeId" placeholder="可选" clearable filterable style="width: 100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :value="e.id" :label="e.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roleOptions" :key="r.id" :value="r.id" :label="r.roleName" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userPage, userAdd, userUpdate, userRemove, userResetPwd, roleList } from '@/api/system'
import { employeePage } from '@/api/employee'
import { fmtDateTime } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const roleOptions = ref([])
const employeeOptions = ref([])
// 角色 id → 中文名 映射，表格里把 roleIds 翻成标签显示
const roleNameMap = computed(() => {
  const m = {}
  roleOptions.value.forEach(r => { m[r.id] = r.roleName })
  return m
})

const query = reactive({ page: 1, size: 10, username: '', realName: '', status: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const form = reactive({
  id: null, username: '', realName: '', phone: '', email: '',
  employeeId: null, roleIds: [], status: 1
})

async function loadData() {
  loading.value = true
  try {
    const res = await userPage(query)
    tableData.value = res.data.list || []
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.username = ''
  query.realName = ''
  query.status = ''
  query.page = 1
  loadData()
}

function handlePageChange(p) {
  query.page = p
  loadData()
}

async function loadOptions() {
  const r = await roleList()
  roleOptions.value = r.data || []
  const e = await employeePage({ page: 1, size: 1000 })
  employeeOptions.value = e.data.list || []
}

function openAdd() {
  dialogTitle.value = '新增用户'
  Object.assign(form, {
    id: null, username: '', realName: '', phone: '', email: '',
    employeeId: null, roleIds: [], status: 1
  })
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑用户'
  Object.assign(form, {
    id: row.id, username: row.username, realName: row.realName,
    phone: row.phone, email: row.email, employeeId: row.employeeId,
    roleIds: row.roleIds ? [...row.roleIds] : [], status: row.status
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.username) { ElMessage.warning('请填写登录账号'); return }
  if (!form.roleIds || form.roleIds.length === 0) { ElMessage.warning('请至少选择一个角色'); return }
  const api = form.id ? userUpdate : userAdd
  await api(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm(`确认将「${row.realName || row.username}」的密码重置为 123456？`, '提示', { type: 'warning' })
  await userResetPwd(row.id)
  ElMessage.success('密码已重置为 123456')
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.realName || row.username}」？`, '提示', { type: 'warning' })
  await userRemove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadOptions()
  loadData()
})
</script>

<style scoped>
.page { padding: 16px; }
.search-bar { background: #fff; padding: 16px; border-radius: 8px; margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>

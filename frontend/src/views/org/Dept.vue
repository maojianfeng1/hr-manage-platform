<template>
  <div class="page">

    <el-form :inline="true" :model="query" class="search-bar">
      <el-form-item label="部门名称">
        <el-input v-model="query.deptName" placeholder="支持模糊查询" clearable @keyup.enter="loadData" />
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
        <el-button type="success" @click="openAdd">新增部门</el-button>
      </el-form-item>
    </el-form>


    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="deptName" label="部门名称" />
      <el-table-column prop="deptCode" label="部门编码" />
      <el-table-column prop="parentName" label="上级部门" />
      <el-table-column prop="leaderName" label="负责人" />
      <el-table-column prop="sort" label="排序" width="80" />
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
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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


    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="部门名称" required>
          <el-input v-model="form.deptName" />
        </el-form-item>
        <el-form-item label="部门编码">
          <el-input v-model="form.deptCode" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-select v-model="form.parentId" placeholder="顶级部门" clearable style="width: 100%">
            <el-option :value="0" label="顶级部门" />
            <el-option v-for="d in deptOptions" :key="d.id" :value="d.id" :label="d.deptName" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.leaderId" placeholder="请选择" clearable filterable style="width: 100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :value="e.id" :label="e.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptPage, deptList, deptAdd, deptUpdate, deptRemove } from '@/api/org'
import { employeePage } from '@/api/employee'
import { fmtDateTime } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 下拉选项
const deptOptions = ref([])
const employeeOptions = ref([])

// 查询条件（字段名与后端 HrDepartmentQueryDTO 一一对应）
const query = reactive({ page: 1, size: 10, deptName: '', status: '' })

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const form = reactive({
  id: null, deptName: '', deptCode: '', parentId: 0,
  leaderId: null, sort: 0, status: 1
})

async function loadData() {
  loading.value = true
  try {
    const res = await deptPage(query)
    // 把 parentId / leaderId 翻译成名称，方便表格展示
    const deptMap = {}
    deptOptions.value.forEach(d => { deptMap[d.id] = d.deptName })
    const empMap = {}
    employeeOptions.value.forEach(e => { empMap[e.id] = e.name })
    tableData.value = (res.data.list || []).map(item => ({
      ...item,
      parentName: item.parentId ? (deptMap[item.parentId] || '—') : '顶级部门',
      leaderName: item.leaderId ? (empMap[item.leaderId] || '—') : '—'
    }))
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.deptName = ''
  query.status = ''
  query.page = 1
  loadData()
}

function handlePageChange(p) {
  query.page = p
  loadData()
}

async function loadOptions() {
  const d = await deptList()
  deptOptions.value = d.data || []
  const e = await employeePage({ page: 1, size: 1000 })
  employeeOptions.value = e.data.list || []
}

function openAdd() {
  dialogTitle.value = '新增部门'
  Object.assign(form, {
    id: null, deptName: '', deptCode: '', parentId: 0,
    leaderId: null, sort: 0, status: 1
  })
  dialogVisible.value = true
}

function openEdit(row) {
  dialogTitle.value = '编辑部门'
  Object.assign(form, {
    id: row.id, deptName: row.deptName, deptCode: row.deptCode,
    parentId: row.parentId || 0, leaderId: row.leaderId,
    sort: row.sort || 0, status: row.status
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.deptName) { ElMessage.warning('请填写部门名称'); return }
  const api = form.id ? deptUpdate : deptAdd
  await api(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除部门「${row.deptName}」？`, '提示', { type: 'warning' })
  await deptRemove(row.id)
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


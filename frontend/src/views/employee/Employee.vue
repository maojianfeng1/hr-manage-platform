<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="search-bar">
      <el-form-item label="工号"><el-input v-model="query.empCode" placeholder="模糊查询" clearable @keyup.enter="loadData" /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="query.name" placeholder="模糊查询" clearable @keyup.enter="loadData" /></el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.deptId" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="d in deptOptions" :key="d.id" :value="d.id" :label="d.deptName" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openAdd">新增员工</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="empCode" label="工号" width="100" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column label="性别" width="70">
        <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '—' }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="deptName" label="部门" />
      <el-table-column prop="postName" label="岗位" />
      <el-table-column prop="jobLevel" label="职级" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 2 ? 'success' : row.status === 1 ? 'warning' : 'info'">
            {{ row.status === 1 ? '试用' : row.status === 2 ? '在职' : '离职' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="入职日期" width="120">
        <template #default="{ row }">{{ fmtDate(row.entryDate) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.size" :current-page="query.page" @current-change="handlePageChange" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="form" label-width="80px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="工号" required><el-input v-model="form.empCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别">
            <el-select v-model="form.gender" style="width: 100%">
              <el-option :value="1" label="男" />
              <el-option :value="2" label="女" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option :value="1" label="试用" /><el-option :value="2" label="在职" /><el-option :value="3" label="离职" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部门">
            <el-select v-model="form.deptId" placeholder="请选择" style="width: 100%">
              <el-option v-for="d in deptOptions" :key="d.id" :value="d.id" :label="d.deptName" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="岗位">
            <el-select v-model="form.postId" placeholder="请选择" style="width: 100%">
              <el-option v-for="p in postOptions" :key="p.id" :value="p.id" :label="p.postName" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="职级"><el-input v-model="form.jobLevel" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="学历"><el-input v-model="form.education" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入职日期"><el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
        </el-row>
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
import { employeePage, employeeAdd, employeeUpdate, employeeRemove } from '@/api/employee'
import { deptList, postPage } from '@/api/org'
import { fmtDate } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const deptOptions = ref([])
const postOptions = ref([])
const query = reactive({ page: 1, size: 10, empCode: '', name: '', deptId: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const emptyForm = () => ({
  id: null, empCode: '', name: '', gender: 1, phone: '', email: '',
  deptId: null, postId: null, jobLevel: '', education: '', entryDate: '', status: 2
})
const form = reactive(emptyForm())

async function loadData() {
  loading.value = true
  try {
    const res = await employeePage(query)
    const deptMap = {}, postMap = {}
    deptOptions.value.forEach(d => { deptMap[d.id] = d.deptName })
    postOptions.value.forEach(p => { postMap[p.id] = p.postName })
    tableData.value = (res.data.list || []).map(item => ({
      ...item,
      deptName: item.deptId ? (deptMap[item.deptId] || '—') : '—',
      postName: item.postId ? (postMap[item.postId] || '—') : '—'
    }))
    total.value = res.data.total
  } finally { loading.value = false }
}
function resetQuery() { query.empCode = ''; query.name = ''; query.deptId = ''; query.page = 1; loadData() }
function handlePageChange(p) { query.page = p; loadData() }
async function loadOptions() {
  const d = await deptList(); deptOptions.value = d.data || []
  const p = await postPage({ page: 1, size: 1000 }); postOptions.value = p.data.list || []
}
function openAdd() { dialogTitle.value = '新增员工'; Object.assign(form, emptyForm()); dialogVisible.value = true }
function openEdit(row) {
  dialogTitle.value = '编辑员工'
  Object.assign(form, {
    id: row.id, empCode: row.empCode, name: row.name, gender: row.gender || 1,
    phone: row.phone, email: row.email, deptId: row.deptId, postId: row.postId,
    jobLevel: row.jobLevel, education: row.education, entryDate: row.entryDate, status: row.status || 2
  })
  dialogVisible.value = true
}
async function submit() {
  if (!form.empCode || !form.name) { ElMessage.warning('工号和姓名必填'); return }
  const api = form.id ? employeeUpdate : employeeAdd
  await api(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除员工「${row.name}」？`, '提示', { type: 'warning' })
  await employeeRemove(row.id)
  ElMessage.success('删除成功')
  loadData()
}
onMounted(() => { loadOptions(); loadData() })
</script>

<style scoped>
.page { padding: 16px; }
.search-bar { background: #fff; padding: 16px; border-radius: 8px; margin-bottom: 12px; }
.pager { margin-top: 12px; justify-content: flex-end; }
</style>

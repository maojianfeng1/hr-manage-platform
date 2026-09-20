<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="search-bar">
      <el-form-item label="岗位名称">
        <el-input v-model="query.postName" placeholder="模糊查询" clearable @keyup.enter="loadData" />
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
        <el-button type="success" @click="openAdd">新增岗位</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="postName" label="岗位名称" />
      <el-table-column prop="postCode" label="岗位编码" />
      <el-table-column prop="deptName" label="所属部门" />
      <el-table-column prop="headcount" label="编制人数" width="100" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.size" :current-page="query.page"
                   @current-change="handlePageChange" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="岗位名称" required><el-input v-model="form.postName" /></el-form-item>
        <el-form-item label="岗位编码"><el-input v-model="form.postCode" /></el-form-item>
        <el-form-item label="所属部门">
          <el-select v-model="form.deptId" placeholder="请选择" clearable style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.id" :value="d.id" :label="d.deptName" />
          </el-select>
        </el-form-item>
        <el-form-item label="编制人数"><el-input-number v-model="form.headcount" :min="0" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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
import { postPage, postAdd, postUpdate, postRemove, deptList } from '@/api/org'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const deptOptions = ref([])
const query = reactive({ page: 1, size: 10, postName: '', status: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const form = reactive({ id: null, postName: '', postCode: '', deptId: null, headcount: 0, remark: '', status: 1 })

async function loadData() {
  loading.value = true
  try {
    const res = await postPage(query)
    const deptMap = {}
    deptOptions.value.forEach(d => { deptMap[d.id] = d.deptName })
    tableData.value = (res.data.list || []).map(item => ({
      ...item, deptName: item.deptId ? (deptMap[item.deptId] || '—') : '—'
    }))
    total.value = res.data.total
  } finally { loading.value = false }
}
function resetQuery() { query.postName = ''; query.status = ''; query.page = 1; loadData() }
function handlePageChange(p) { query.page = p; loadData() }
async function loadOptions() { const d = await deptList(); deptOptions.value = d.data || [] }
function openAdd() {
  dialogTitle.value = '新增岗位'
  Object.assign(form, { id: null, postName: '', postCode: '', deptId: null, headcount: 0, remark: '', status: 1 })
  dialogVisible.value = true
}
function openEdit(row) {
  dialogTitle.value = '编辑岗位'
  Object.assign(form, { id: row.id, postName: row.postName, postCode: row.postCode, deptId: row.deptId, headcount: row.headcount || 0, remark: row.remark, status: row.status })
  dialogVisible.value = true
}
async function submit() {
  if (!form.postName) { ElMessage.warning('请填写岗位名称'); return }
  const api = form.id ? postUpdate : postAdd
  await api(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除岗位「${row.postName}」？`, '提示', { type: 'warning' })
  await postRemove(row.id)
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

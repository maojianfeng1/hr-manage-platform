<template>
  <div class="page">
    <el-form :inline="true" :model="query" class="search-bar">
      <el-form-item label="员工姓名"><el-input v-model="query.empName" placeholder="模糊查询" clearable @keyup.enter="loadData" /></el-form-item>
      <el-form-item label="月份">
        <el-date-picker v-model="query.month" type="month" value-format="YYYY-MM" placeholder="选择月份" style="width: 150px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="(label, val) in ATT_STATUS_MAP" :key="val" :value="val" :label="label" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="success" @click="openAdd">录入考勤</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="empName" label="姓名" width="100" />
      <el-table-column label="考勤日期" width="120">
        <template #default="{ row }">{{ fmtDate(row.attendDate) }}</template>
      </el-table-column>
      <el-table-column label="上班" width="90"><template #default="{ row }">{{ fmtTime(row.clockIn) }}</template></el-table-column>
      <el-table-column label="下班" width="90"><template #default="{ row }">{{ fmtTime(row.clockOut) }}</template></el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'NORMAL' ? 'success' : row.status === 'OVERTIME' ? 'warning' : 'danger'">
            {{ ATT_STATUS_MAP[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="overtimeHours" label="加班(h)" width="90" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.size" :current-page="query.page" @current-change="handlePageChange" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="员工" required>
          <el-select v-model="form.employeeId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :value="e.id" :label="`${e.name}（${e.empCode}）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤日期" required>
          <el-date-picker v-model="form.attendDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" required>
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="(label, val) in ATT_STATUS_MAP" :key="val" :value="val" :label="label" />
          </el-select>
        </el-form-item>
        <el-form-item label="上班时间"><el-time-picker v-model="form.clockIn" value-format="HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="下班时间"><el-time-picker v-model="form.clockOut" value-format="HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="加班时长" v-if="form.status === 'OVERTIME'">
          <el-input-number v-model="form.overtimeHours" :min="0" :step="0.5" :precision="1" />
        </el-form-item>
        <el-form-item label="请假类型" v-if="form.status === 'LEAVE'">
          <el-select v-model="form.leaveType" style="width: 100%">
            <el-option :value="1" label="事假" /><el-option :value="2" label="病假" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
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
import { attendancePage, attendanceAdd, attendanceUpdate, attendanceRemove } from '@/api/attendance'
import { employeePage } from '@/api/employee'
import { fmtDate } from '@/utils/format'

const ATT_STATUS_MAP = {
  NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '旷工', LEAVE: '请假', OVERTIME: '加班'
}
function fmtTime(v) { return v ? String(v).slice(0, 8) : '—' }

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const employeeOptions = ref([])
const query = reactive({ page: 1, size: 10, empName: '', month: '', status: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('录入考勤')
const emptyForm = () => ({
  id: null, employeeId: null, attendDate: '', status: 'NORMAL',
  clockIn: '', clockOut: '', overtimeHours: 0, leaveType: 1, remark: ''
})
const form = reactive(emptyForm())

async function loadData() {
  loading.value = true
  try {
    const res = await attendancePage(query)
    tableData.value = res.data.list || []
    total.value = res.data.total
  } finally { loading.value = false }
}
function resetQuery() { query.empName = ''; query.month = ''; query.status = ''; query.page = 1; loadData() }
function handlePageChange(p) { query.page = p; loadData() }
async function loadOptions() { const e = await employeePage({ page: 1, size: 1000 }); employeeOptions.value = e.data.list || [] }
function openAdd() { dialogTitle.value = '录入考勤'; Object.assign(form, emptyForm()); dialogVisible.value = true }
function openEdit(row) {
  dialogTitle.value = '编辑考勤'
  Object.assign(form, {
    id: row.id, employeeId: row.employeeId, attendDate: row.attendDate, status: row.status,
    clockIn: row.clockIn, clockOut: row.clockOut, overtimeHours: row.overtimeHours || 0,
    leaveType: row.leaveType || 1, remark: row.remark
  })
  dialogVisible.value = true
}
async function submit() {
  if (!form.employeeId || !form.attendDate) { ElMessage.warning('请选择员工和考勤日期'); return }
  const api = form.id ? attendanceUpdate : attendanceAdd
  await api(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该考勤记录？', '提示', { type: 'warning' })
  await attendanceRemove(row.id)
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

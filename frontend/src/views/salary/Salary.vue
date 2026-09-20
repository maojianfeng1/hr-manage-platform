<template>
  <div class="page">
    <el-tabs v-model="activeTab">

      <el-tab-pane label="薪资档案" name="standard">
        <el-form :inline="true" class="search-bar">
          <el-form-item><el-button type="success" @click="openAdd">新建薪资档案</el-button></el-form-item>
        </el-form>
        <el-table :data="stdData" border stripe v-loading="stdLoading">
          <el-table-column prop="employeeId" label="员工ID" width="90" />
          <el-table-column prop="empName" label="姓名" width="100" />
          <el-table-column label="基本工资" width="110"><template #default="{row}">{{ money(row.baseSalary) }}</template></el-table-column>
          <el-table-column label="岗位工资" width="110"><template #default="{row}">{{ money(row.postSalary) }}</template></el-table-column>
          <el-table-column label="绩效工资" width="110"><template #default="{row}">{{ money(row.perfSalary) }}</template></el-table-column>
          <el-table-column prop="bankCard" label="工资卡号" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>


      <el-tab-pane label="工资单" name="payroll">
        <el-form :inline="true" class="search-bar">
          <el-form-item label="月份">
            <el-date-picker v-model="genMonth" type="month" value-format="YYYY-MM" placeholder="如 2026-09" style="width: 150px" />
          </el-form-item>
          <el-form-item><el-button type="warning" @click="generate">生成工资单</el-button></el-form-item>
        </el-form>
        <el-table :data="payData" border stripe v-loading="payLoading">
          <el-table-column prop="empName" label="姓名" width="100" />
          <el-table-column prop="deptName" label="部门" width="120" />
          <el-table-column prop="postName" label="岗位" width="120" />
          <el-table-column prop="salaryMonth" label="薪资月份" width="110" />
          <el-table-column label="应发" width="110"><template #default="{row}">{{ money(row.grossPay) }}</template></el-table-column>
          <el-table-column label="实发" width="110"><template #default="{row}">{{ money(row.netPay) }}</template></el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>


    <el-dialog v-model="stdVisible" :title="stdTitle" width="520px">
      <el-form :model="stdForm" label-width="90px">
        <el-form-item label="员工" required>
          <el-select v-model="stdForm.employeeId" placeholder="请选择" filterable style="width: 100%">
            <el-option v-for="e in employeeOptions" :key="e.id" :value="e.id" :label="`${e.name}（${e.empCode}）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="基本工资"><el-input-number v-model="stdForm.baseSalary" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="岗位工资"><el-input-number v-model="stdForm.postSalary" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="绩效工资"><el-input-number v-model="stdForm.perfSalary" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="社保基数"><el-input-number v-model="stdForm.socialBase" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="公积金基数"><el-input-number v-model="stdForm.fundBase" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="工资卡号"><el-input v-model="stdForm.bankCard" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stdVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  salaryStandardPage, salaryStandardSave,
  payrollPage, payrollGenerate, payrollRemove
} from '@/api/salary'
import { employeePage } from '@/api/employee'

const money = (v) => (v == null ? '—' : '¥' + Number(v).toFixed(2))

const activeTab = ref('standard')
const employeeOptions = ref([])

// 薪资档案
const stdLoading = ref(false)
const stdData = ref([])
const stdVisible = ref(false)
const stdTitle = ref('新建薪资档案')
const stdForm = reactive({ id: null, employeeId: null, baseSalary: 0, postSalary: 0, perfSalary: 0, socialBase: 0, fundBase: 0, bankCard: '' })

// 工资单
const payLoading = ref(false)
const payData = ref([])
const genMonth = ref('2026-09')

async function loadStd() {
  stdLoading.value = true
  try {
    const res = await salaryStandardPage({ page: 1, size: 10 })
    const empMap = {}
    employeeOptions.value.forEach(e => { empMap[e.id] = e.name })
    stdData.value = (res.data.list || []).map(item => ({ ...item, empName: empMap[item.employeeId] || '—' }))
  } finally { stdLoading.value = false }
}
async function loadPay() {
  payLoading.value = true
  try {
    const res = await payrollPage({ page: 1, size: 10 })
    payData.value = res.data.list || []
  } finally { payLoading.value = false }
}
function openAdd() {
  stdTitle.value = '新建薪资档案'
  Object.assign(stdForm, { id: null, employeeId: null, baseSalary: 0, postSalary: 0, perfSalary: 0, socialBase: 0, fundBase: 0, bankCard: '' })
  stdVisible.value = true
}
async function openEdit(row) {
  stdTitle.value = '编辑薪资档案'
  // 列表已返回薪资档案完整字段，直接填充即可（后端薪资档案无按 id 的详情接口）
  Object.assign(stdForm, {
    id: row.id ?? null, employeeId: row.employeeId,
    baseSalary: row.baseSalary || 0, postSalary: row.postSalary || 0,
    perfSalary: row.perfSalary || 0, socialBase: row.socialBase || 0,
    fundBase: row.fundBase || 0, bankCard: row.bankCard || ''
  })
  stdVisible.value = true
}

async function saveStd() {
  if (!stdForm.employeeId) { ElMessage.warning('请选择员工'); return }
  await salaryStandardSave(stdForm)
  ElMessage.success('保存成功')
  stdVisible.value = false
  loadStd()
}
async function generate() {
  if (!genMonth.value) { ElMessage.warning('请选择月份'); return }
  await payrollGenerate(genMonth.value)
  ElMessage.success('生成完成')
  loadPay()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该工资单？', '提示', { type: 'warning' })
  await payrollRemove(row.id)
  ElMessage.success('删除成功')
  loadPay()
}
async function loadOptions() { const e = await employeePage({ page: 1, size: 1000 }); employeeOptions.value = e.data.list || [] }
onMounted(() => { loadOptions(); loadStd(); loadPay() })
</script>

<style scoped>
.page { padding: 16px; }
.search-bar { background: #fff; padding: 16px; border-radius: 8px; margin-bottom: 12px; }
</style>

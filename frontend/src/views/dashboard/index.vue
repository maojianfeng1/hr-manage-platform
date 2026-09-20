<template>
  <div class="dashboard">

    <!-- ===== 公司级看板（admin / 人事专员：拥有 emp:view 等公司级权限） ===== -->
    <template v-if="isCompany">
      <el-row :gutter="16" class="kpi-row">
        <el-col :span="6" v-if="userStore.hasPerm('emp:view')"><el-card shadow="hover"><div class="kpi-label">员工总数</div><div class="kpi-value">{{ kpi.empTotal }}</div></el-card></el-col>
        <el-col :span="6" v-if="userStore.hasPerm('org:dept:view')"><el-card shadow="hover"><div class="kpi-label">部门数</div><div class="kpi-value">{{ kpi.deptTotal }}</div></el-card></el-col>
        <el-col :span="6" v-if="userStore.hasPerm('emp:view')"><el-card shadow="hover"><div class="kpi-label">在职员工</div><div class="kpi-value">{{ kpi.activeEmp }}</div></el-card></el-col>
        <el-col :span="6" v-if="userStore.hasPerm('sal:view')"><el-card shadow="hover"><div class="kpi-label">本月工资单</div><div class="kpi-value">{{ kpi.payCount }}</div></el-card></el-col>
      </el-row>

      <el-row :gutter="16" class="chart-row">
        <el-col :span="8" v-if="userStore.hasPerm('org:dept:view')"><el-card shadow="never"><div ref="pieDept" class="chart"></div></el-card></el-col>
        <el-col :span="8" v-if="userStore.hasPerm('att:view')"><el-card shadow="never"><div ref="pieAtt" class="chart"></div></el-card></el-col>
        <el-col :span="8" v-if="userStore.hasPerm('sal:view')"><el-card shadow="never"><div ref="barSalary" class="chart"></div></el-card></el-col>
      </el-row>
    </template>

    <!-- ===== 个人看板（普通员工：仅有 dashboard:view，看自己的数据） ===== -->
    <template v-else>
      <el-row :gutter="16" class="kpi-row">
        <el-col :span="6"><el-card shadow="hover"><div class="kpi-label">本月考勤天数</div><div class="kpi-value">{{ pkpi.attendDays }}</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><div class="kpi-label">本月异常</div><div class="kpi-value">{{ pkpi.abnormal }}</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><div class="kpi-label">最近工资月份</div><div class="kpi-value">{{ pkpi.lastMonth }}</div></el-card></el-col>
        <el-col :span="6"><el-card shadow="hover"><div class="kpi-label">实发工资</div><div class="kpi-value">{{ pkpi.netPay }}</div></el-card></el-col>
      </el-row>

      <el-row :gutter="16" class="chart-row">
        <el-col :span="10"><el-card shadow="never"><div ref="pieAtt" class="chart"></div></el-card></el-col>
        <el-col :span="14">
          <el-card shadow="never">
            <div class="kpi-label" style="margin-bottom:8px">我的员工档案</div>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
              <el-descriptions-item label="工号">{{ profile.empCode }}</el-descriptions-item>
              <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
              <el-descriptions-item label="电话">{{ profile.phone }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ statusText }}</el-descriptions-item>
              <el-descriptions-item label="入职日期">{{ profile.entryDate }}</el-descriptions-item>
            </el-descriptions>

            <div class="kpi-label" style="margin:12px 0 8px">最近工资单明细</div>
            <el-descriptions :column="2" border size="small" v-if="payList.length">
              <el-descriptions-item label="月份">{{ payList[0].salaryMonth }}</el-descriptions-item>
              <el-descriptions-item label="应发">{{ payList[0].grossPay }}</el-descriptions-item>
              <el-descriptions-item label="社保个人">{{ payList[0].socialPersonal }}</el-descriptions-item>
              <el-descriptions-item label="公积金个人">{{ payList[0].fundPersonal }}</el-descriptions-item>
              <el-descriptions-item label="个税">{{ payList[0].tax }}</el-descriptions-item>
              <el-descriptions-item label="实发">{{ payList[0].netPay }}</el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无工资单" :image-size="60"></el-empty>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/stores/user'
import { employeePage } from '@/api/employee'
import { deptList } from '@/api/org'
import { attendancePage } from '@/api/attendance'
import { payrollPage } from '@/api/salary'
import { myInfo, myAttendance, myPayroll } from '@/api/personal'

const userStore = useUserStore()

// 是否拥有公司级权限：以 emp:view 作为「管理视角」标记
const isCompany = computed(() => userStore.hasPerm('emp:view'))

/* ---------- 公司级看板数据 ---------- */
const kpi = ref({ empTotal: 0, deptTotal: 0, activeEmp: 0, payCount: 0 })
const pieDept = ref(null)
const barSalary = ref(null)

/* ---------- 个人看板数据 ---------- */
const profile = ref({})
const payList = ref([])
const pkpi = ref({ attendDays: 0, abnormal: 0, lastMonth: '-', netPay: 0 })

const pieAtt = ref(null)
let charts = []

const ATT_MAP = { NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '旷工', LEAVE: '请假', OVERTIME: '加班' }
const curMonth = () => { const d = new Date(); const m = String(d.getMonth() + 1).padStart(2, '0'); return `${d.getFullYear()}-${m}` }

const genderText = computed(() => profile.value.gender === 2 ? '女' : '男')
const statusText = computed(() => ({ 1: '试用', 2: '在职', 3: '离职' }[profile.value.status] || '未知'))

function clearCharts() { charts.forEach(c => c.dispose()); charts = [] }

async function loadCompany() {
  clearCharts()
  const empList      = userStore.hasPerm('emp:view')      ? (await employeePage({ page: 1, size: 1000 })).data.list || [] : []
  const deptListData = userStore.hasPerm('org:dept:view') ? (await deptList()).data || [] : []
  const attList      = userStore.hasPerm('att:view')      ? (await attendancePage({ page: 1, size: 1000, month: curMonth() })).data.list || [] : []
  const payList      = userStore.hasPerm('sal:view')      ? (await payrollPage({ page: 1, size: 1000 })).data.list || [] : []

  kpi.value = {
    empTotal: empList.length,
    deptTotal: deptListData.length,
    activeEmp: empList.filter(e => e.status === 2).length,
    payCount: payList.length
  }

  if (userStore.hasPerm('org:dept:view')) {
    const deptMap = {}; deptListData.forEach(d => { deptMap[d.id] = d.deptName })
    const deptCount = {}
    empList.forEach(e => { const name = e.deptId ? (deptMap[e.deptId] || '未分配') : '未分配'; deptCount[name] = (deptCount[name] || 0) + 1 })
    renderPie(pieDept.value, '部门人数分布', Object.keys(deptCount).map(k => ({ name: k, value: deptCount[k] })))
  }
  if (userStore.hasPerm('att:view')) {
    const attCount = {}
    attList.forEach(a => { const name = ATT_MAP[a.status] || a.status; attCount[name] = (attCount[name] || 0) + 1 })
    renderPie(pieAtt.value, '考勤状态分布', Object.keys(attCount).map(k => ({ name: k, value: attCount[k] })))
  }
  if (userStore.hasPerm('sal:view')) {
    const salarySum = {}
    payList.forEach(p => { salarySum[p.salaryMonth] = (salarySum[p.salaryMonth] || 0) + Number(p.netPay || 0) })
    renderBar(barSalary.value, '月度薪酬成本', Object.keys(salarySum), Object.values(salarySum))
  }
}

async function loadPersonal() {
  clearCharts()
  const info = (await myInfo()).data || {}
  profile.value = info

  const att = (await myAttendance(curMonth())).data || []
  const pay = (await myPayroll()).data || []
  payList.value = pay

  pkpi.value = {
    attendDays: att.filter(a => a.status !== 'LEAVE').length,
    abnormal: att.filter(a => ['LATE', 'EARLY', 'ABSENT'].includes(a.status)).length,
    lastMonth: pay.length ? pay[0].salaryMonth : '-',
    netPay: pay.length ? pay[0].netPay : 0
  }

  if (att.length) {
    const attCount = {}
    att.forEach(a => { const name = ATT_MAP[a.status] || a.status; attCount[name] = (attCount[name] || 0) + 1 })
    renderPie(pieAtt.value, '本月考勤状态', Object.keys(attCount).map(k => ({ name: k, value: attCount[k] })))
  }
}

function renderPie(el, title, data) {
  const c = echarts.init(el); charts.push(c)
  c.setOption({
    title: { text: title, left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: '55%', data, label: { formatter: '{b}: {c}' } }]
  })
}
function renderBar(el, title, xData, yData) {
  const c = echarts.init(el); charts.push(c)
  c.setOption({
    title: { text: title, left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: xData },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: yData, itemStyle: { color: '#185fa5' } }]
  })
}

function resizeAll() { charts.forEach(c => c.resize()) }
window.addEventListener('resize', resizeAll)

onMounted(async () => {
  // 刷新后 Pinia 内存态清空，先确保 perms 已加载再按权限分支，避免误判
  if (!userStore.perms.length) {
    try { await userStore.fetchUserInfo() } catch (e) { /* 忽略 */ }
  }
  if (isCompany.value) await loadCompany()
  else await loadPersonal()
})
onBeforeUnmount(() => { charts.forEach(c => c.dispose()); window.removeEventListener('resize', resizeAll) })
</script>

<style scoped>
.dashboard { padding: 16px; }
.kpi-row { margin-bottom: 16px; }
.kpi-label { color: #8f9bb3; font-size: 13px; }
.kpi-value { font-size: 28px; font-weight: 600; color: #1f2d3d; margin-top: 6px; }
.chart { height: 320px; }
</style>

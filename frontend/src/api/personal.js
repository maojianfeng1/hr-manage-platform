import request from './request'

// 个人自助接口：只返回「当前登录用户本人」的数据，无需公司级权限
export function myInfo() {
  return request.get('/personal/my-info')
}
export function myAttendance(month) {
  return request.get('/personal/my-attendance', { params: month ? { month } : {} })
}
export function myPayroll() {
  return request.get('/personal/my-payroll')
}

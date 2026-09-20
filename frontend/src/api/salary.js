import request from './request'

// 薪资档案（标准）：只有 save（后端 upsert），无独立 update/delete
export function salaryStandardPage(params) {
  return request.get('/salary/standard/page', { params })
}
export function salaryStandardByEmployee(employeeId) {
  return request.get(`/salary/standard/${employeeId}`)
}
export function salaryStandardSave(data) {
  return request.post('/salary/standard', data)
}

// 工资单
export function payrollPage(params) {
  return request.get('/salary/payroll/page', { params })
}
export function payrollGenerate(month) {
  return request.post(`/salary/payroll/generate?month=${month}`)
}
export function payrollDetail(id) {
  return request.get(`/salary/payroll/${id}`)
}
export function payrollRemove(id) {
  return request.delete(`/salary/payroll/${id}`)
}

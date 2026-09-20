import request from './request'

export function employeePage(params) {
  return request.get('/employee/page', { params })
}
export function employeeDetail(id) {
  return request.get(`/employee/${id}`)
}
export function employeeAdd(data) {
  return request.post('/employee', data)
}
export function employeeUpdate(data) {
  return request.put('/employee', data)
}
export function employeeRemove(id) {
  return request.delete(`/employee/${id}`)
}

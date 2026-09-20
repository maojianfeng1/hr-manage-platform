import request from './request'

export function attendancePage(params) {
  return request.get('/attendance/page', { params })
}
export function attendanceDetail(id) {
  return request.get(`/attendance/${id}`)
}
export function attendanceAdd(data) {
  return request.post('/attendance', data)
}
export function attendanceUpdate(data) {
  return request.put('/attendance', data)
}
export function attendanceRemove(id) {
  return request.delete(`/attendance/${id}`)
}

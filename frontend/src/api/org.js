import request from './request'

// ===== 部门 =====
export function deptPage(params) {
  return request.get('/org/dept/page', { params })
}
export function deptList() {
  return request.get('/org/dept/list')
}
export function deptDetail(id) {
  return request.get(`/org/dept/${id}`)
}
export function deptAdd(data) {
  return request.post('/org/dept', data)
}
export function deptUpdate(data) {
  return request.put('/org/dept', data)
}
export function deptRemove(id) {
  return request.delete(`/org/dept/${id}`)
}

// ===== 岗位 =====
export function postPage(params) {
  return request.get('/org/post/page', { params })
}
export function postDetail(id) {
  return request.get(`/org/post/${id}`)
}
export function postAdd(data) {
  return request.post('/org/post', data)
}
export function postUpdate(data) {
  return request.put('/org/post', data)
}
export function postRemove(id) {
  return request.delete(`/org/post/${id}`)
}

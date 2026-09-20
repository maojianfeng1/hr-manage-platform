import request from './request'

// ============ 用户管理 ============
export function userPage(params) {
  return request.get('/system/user/page', { params })
}
export function userDetail(id) {
  return request.get(`/system/user/${id}`)
}
export function userAdd(data) {
  return request.post('/system/user', data)
}
export function userUpdate(data) {
  return request.put('/system/user', data)
}
export function userRemove(id) {
  return request.delete(`/system/user/${id}`)
}
export function userResetPwd(id) {
  return request.post(`/system/user/${id}/reset-pwd`)
}

// ============ 角色管理 ============
export function roleList() {
  return request.get('/system/role/list')
}
export function roleDetail(id) {
  return request.get(`/system/role/${id}`)
}
export function roleAdd(data) {
  return request.post('/system/role', data)
}
export function roleUpdate(data) {
  return request.put('/system/role', data)
}
export function roleRemove(id) {
  return request.delete(`/system/role/${id}`)
}
export function roleAssignMenus(roleId, menuIds) {
  return request.post(`/system/role/${roleId}/menus`, menuIds)
}

// ============ 菜单树（角色授权用） ============
export function menuTree() {
  return request.get('/menu/tree')
}
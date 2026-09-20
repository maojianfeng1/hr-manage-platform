import request from './request'

/**
 * 菜单接口
 * - getMenus：侧边栏用，按当前登录用户角色返回可见菜单树
 * - getMenuList：菜单管理页用，管理员可见全部菜单树（含按钮权限点）
 */
export function getMenus() {
  return request.get('/menu/user-menus')
}

export function getMenuList() {
  return request.get('/menu/list')
}

export function saveMenu(data) {
  return request.post('/menu/save', data)
}

export function updateMenu(data) {
  return request.put('/menu/update', data)
}

export function removeMenu(id) {
  return request.delete(`/menu/remove/${id}`)
}

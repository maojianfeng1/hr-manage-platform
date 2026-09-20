package com.hr.service;
import com.hr.dto.SysRoleSaveDTO;
import com.hr.vo.SysRoleVO;

import java.util.List;

public interface SysRoleService {
    List<SysRoleVO> listAll();
    SysRoleVO detail(Long id);
    void add(SysRoleSaveDTO dto);
    void update(SysRoleSaveDTO dto);
    void remove(Long id);
    /** 给角色授权菜单（全量覆盖式） */
    void assignMenus(Long roleId, List<Long> menuIds);
}

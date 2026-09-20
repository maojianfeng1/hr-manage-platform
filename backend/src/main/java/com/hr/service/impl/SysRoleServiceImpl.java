package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.dto.SysRoleSaveDTO;
import com.hr.entity.SysRole;
import com.hr.mapper.SysRoleMapper;
import com.hr.service.SysRoleService;
import com.hr.vo.SysRoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;

    public SysRoleServiceImpl(SysRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public List<SysRoleVO> listAll() {
        return roleMapper.selectAll().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public SysRoleVO detail(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) throw new BusinessException("角色不存在");
        return toVO(role);
    }

    @Override
    @Transactional
    public void add(SysRoleSaveDTO dto) {
        if (dto.getRoleKey() == null || dto.getRoleKey().isBlank())
            throw new BusinessException("角色标识不能为空");
        if (roleMapper.countByKey(dto.getRoleKey(), null) > 0)
            throw new BusinessException("角色标识已存在");

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        roleMapper.insert(role);
    }

    @Override
    @Transactional
    public void update(SysRoleSaveDTO dto) {
        if (dto.getId() == null) throw new BusinessException("ID 不能为空");
        SysRole exist = roleMapper.selectById(dto.getId());
        if (exist == null) throw new BusinessException("角色不存在");
        if (roleMapper.countByKey(dto.getRoleKey(), dto.getId()) > 0)
            throw new BusinessException("角色标识已存在");

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        roleMapper.update(role);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        if (roleMapper.selectById(id) == null) throw new BusinessException("角色不存在");
        roleMapper.deleteUserRoleByRoleId(id);  // 先解绑用户
        roleMapper.deleteRoleMenu(id);           // 再解绑菜单
        roleMapper.logicDelete(id);              // 最后逻辑删除角色
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        if (roleMapper.selectById(roleId) == null) throw new BusinessException("角色不存在");
        roleMapper.deleteRoleMenu(roleId);   // 先清空旧授权
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                roleMapper.insertRoleMenu(roleId, menuId);
            }
        }
    }

    private SysRoleVO toVO(SysRole r) {
        SysRoleVO vo = new SysRoleVO();
        BeanUtils.copyProperties(r, vo);
        vo.setMenuIds(roleMapper.selectMenuIdsByRoleId(r.getId()));
        return vo;
    }
}

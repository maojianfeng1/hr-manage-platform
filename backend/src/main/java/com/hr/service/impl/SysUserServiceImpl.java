package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.dto.SysUserQueryDTO;
import com.hr.dto.SysUserSaveDTO;
import com.hr.entity.SysUser;
import com.hr.mapper.SysUserMapper;
import com.hr.service.SysUserService;
import com.hr.vo.SysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysUserServiceImpl implements SysUserService {

    /** 新增用户的默认密码，和阶段④初始化器保持一致 */
    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(SysUserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<SysUserVO> page(SysUserQueryDTO q) {
        int size = q.getSize() == null ? 10 : q.getSize();
        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int offset = (page - 1) * size;
        List<SysUser> list = userMapper.selectUserPage(q, offset);
        long total = userMapper.countUser(q);
        List<SysUserVO> voList = list.stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(total, voList);
    }

    @Override
    public SysUserVO detail(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return toVO(user);
    }

    @Override
    @Transactional   // 同时写 sys_user 和 sys_user_role，必须原子
    public void add(SysUserSaveDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new BusinessException("登录账号不能为空");
        if (userMapper.countUsername(dto.getUsername(), null) > 0)
            throw new BusinessException("登录账号已存在");

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);   // 只拷用户名/姓名/手机等，password 不在此处
        if (user.getStatus() == null) user.setStatus(1);
        // 关键：新增用户默认密码 123456，必须 BCrypt 加密入库，绝不能明文
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userMapper.insert(user);

        bindRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional
    public void update(SysUserSaveDTO dto) {
        if (dto.getId() == null) throw new BusinessException("ID 不能为空");
        SysUser exist = userMapper.selectById(dto.getId());
        if (exist == null) throw new BusinessException("用户不存在");
        if (userMapper.countUsername(dto.getUsername(), dto.getId()) > 0)
            throw new BusinessException("登录账号已存在");

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        userMapper.updateUser(user);

        // 重新绑定角色：先清空再插入，保证「全量覆盖」
        bindRoles(dto.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional
    public void remove(Long id) {
        if (userMapper.selectById(id) == null) throw new BusinessException("用户不存在");
        userMapper.deleteUserRole(id);    // 先解绑角色
        userMapper.logicDeleteUser(id);   // 再逻辑删除
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        SysUser exist = userMapper.selectById(id);
        if (exist == null) throw new BusinessException("用户不存在");
        userMapper.updatePassword(id, passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    /** 绑定角色：先清空该用户旧角色，再批量插入新角色 */
    private void bindRoles(Long userId, List<Long> roleIds) {
        userMapper.deleteUserRole(userId);
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                userMapper.insertUserRole(userId, roleId);
            }
        }
    }

    private SysUserVO toVO(SysUser u) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(u, vo);   // 自动跳过 password/avatar 等 VO 没有的字段
        vo.setRoleIds(userMapper.selectRoleIdsByUserId(u.getId()));
        return vo;
    }
}

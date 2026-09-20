package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.context.UserContext;
import com.hr.dto.HrEmployeeQueryDTO;
import com.hr.entity.HrEmployee;
import com.hr.entity.SysUser;
import com.hr.mapper.HrEmployeeMapper;
import com.hr.mapper.SysUserMapper;
import com.hr.service.HrEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HrEmployeeServiceImpl implements HrEmployeeService {

    private final HrEmployeeMapper mapper;
    private final SysUserMapper sysUserMapper;

    public HrEmployeeServiceImpl(HrEmployeeMapper mapper, SysUserMapper sysUserMapper) {
        this.mapper = mapper;
        this.sysUserMapper = sysUserMapper;
    }

    /** 当前登录用户若是普通员工，返回其 employee_id；否则返回 null */
    private Long selfEmployeeId() {
        if (UserContext.get() == null || UserContext.get().getRoles() == null) return null;
        if (!UserContext.get().getRoles().contains("employee")) return null;
        SysUser u = sysUserMapper.selectById(UserContext.getUserId());
        return u == null ? null : u.getEmployeeId();
    }

    @Override
    public PageResult<HrEmployee> page(HrEmployeeQueryDTO q) {
        // 员工只能看自己
        Long selfId = selfEmployeeId();
        if (selfId != null) q.setEmployeeId(selfId);
        int size = q.getSize() == null ? 10 : q.getSize();
        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int offset = (page - 1) * size;
        var list = mapper.selectPage(q, offset);
        long total = mapper.selectCount(q);
        return PageResult.of(total, list);
    }

    @Override
    public HrEmployee detail(Long id) {
        HrEmployee e = mapper.selectById(id);
        if (e == null) throw new BusinessException("员工不存在");
        return e;
    }

    @Override
    public void add(HrEmployee e) {
        if (e.getStatus() == null) e.setStatus(2); // 默认"在职"
        if (e.getGender() == null) e.setGender(1); // 默认男
        mapper.insert(e);
    }

    @Override
    public void update(HrEmployee e) {
        if (e.getId() == null) throw new BusinessException("ID 不能为空");
        if (mapper.selectById(e.getId()) == null) throw new BusinessException("员工不存在");
        mapper.update(e);
    }

    @Override
    public void remove(Long id) {
        if (mapper.selectById(id) == null) throw new BusinessException("员工不存在");
        mapper.logicDelete(id);
    }
}

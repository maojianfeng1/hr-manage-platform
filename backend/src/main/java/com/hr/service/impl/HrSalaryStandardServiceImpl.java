package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.context.UserContext;
import com.hr.dto.HrSalaryStandardQueryDTO;
import com.hr.entity.HrSalaryStandard;
import com.hr.entity.SysUser;
import com.hr.mapper.HrSalaryStandardMapper;
import com.hr.mapper.SysUserMapper;
import com.hr.service.HrSalaryStandardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HrSalaryStandardServiceImpl implements HrSalaryStandardService {

    private final HrSalaryStandardMapper mapper;
    private final SysUserMapper sysUserMapper;

    public HrSalaryStandardServiceImpl(HrSalaryStandardMapper mapper, SysUserMapper sysUserMapper) {
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
    public PageResult<HrSalaryStandard> page(HrSalaryStandardQueryDTO q) {
        // 员工只能看自己的薪资档案（薪酬保密）
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
    public HrSalaryStandard getByEmployeeId(Long employeeId) {
        HrSalaryStandard s = mapper.selectByEmployeeId(employeeId);
        if (s == null) throw new BusinessException("该员工尚未设置薪资档案");
        return s;
    }

    /**
     * upsert：每员工只允许一份档案。
     * 知识点：为什么不用 insert 就完事？因为前端「编辑薪资档案」和「首次建档」是同一个页面，
     * 让调用方不用关心是否存在，由后端判断——这叫幂等，重复点保存不会产生两条脏数据。
     */
    @Override
    public void save(HrSalaryStandard s) {
        if (s.getEmployeeId() == null) throw new BusinessException("员工ID不能为空");
        HrSalaryStandard old = mapper.selectByEmployeeId(s.getEmployeeId());
        if (old == null) {
            mapper.insert(s);
        } else {
            s.setId(old.getId());
            mapper.update(s);
        }
    }
}


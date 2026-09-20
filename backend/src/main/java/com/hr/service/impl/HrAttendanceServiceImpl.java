package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.context.UserContext;
import com.hr.dto.HrAttendanceQueryDTO;
import com.hr.entity.HrAttendance;
import com.hr.entity.HrEmployee;
import com.hr.entity.SysUser;
import com.hr.mapper.HrAttendanceMapper;
import com.hr.mapper.HrEmployeeMapper;
import com.hr.mapper.SysUserMapper;
import com.hr.service.HrAttendanceService;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HrAttendanceServiceImpl implements HrAttendanceService {

    private final HrAttendanceMapper mapper;
    private final HrEmployeeMapper employeeMapper;
    private final SysUserMapper sysUserMapper;

    public HrAttendanceServiceImpl(HrAttendanceMapper mapper,
                                   HrEmployeeMapper employeeMapper,
                                   SysUserMapper sysUserMapper) {
        this.mapper = mapper;
        this.employeeMapper = employeeMapper;
        this.sysUserMapper = sysUserMapper;
    }

    /** 当前登录用户若是普通员工，返回其 employee_id；否则返回 null */
    private Long selfEmployeeId() {
        if (UserContext.get() == null || UserContext.get().getRoles() == null) return null;
        if (!UserContext.get().getRoles().contains("employee")) return null;
        SysUser u = sysUserMapper.selectById(UserContext.getUserId());
        return u == null ? null : u.getEmployeeId();
    }

    /**
     * 填姓名/部门快照：前端只传 employeeId，姓名与部门由后端从员工表取。
     * 知识点：这叫「冗余快照」——考勤是历史数据，员工以后改了名/调了部门，
     * 历史考勤仍应保持当时的姓名部门，所以存快照而不是每次 JOIN 现查。
     */
    private void fillSnapshot(HrAttendance a) {
        if (a.getEmployeeId() == null) throw new BusinessException("员工ID不能为空");
        HrEmployee emp = employeeMapper.selectById(a.getEmployeeId());
        if (emp == null) throw new BusinessException("员工不存在");
        a.setEmpName(emp.getName());
        a.setDeptId(emp.getDeptId());
    }

    @Override
    public PageResult<HrAttendance> page(HrAttendanceQueryDTO q) {
        // 员工只看自己的考勤
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
    public HrAttendance detail(Long id) {
        HrAttendance a = mapper.selectById(id);
        if (a == null) throw new BusinessException("考勤记录不存在");
        return a;
    }

    @Override
    public void add(HrAttendance a) {
        fillSnapshot(a);
        if (a.getStatus() == null) a.setStatus("NORMAL");
        if (a.getOvertimeHours() == null) a.setOvertimeHours(BigDecimal.ZERO);
        mapper.insert(a);
    }

    @Override
    public void update(HrAttendance a) {
        if (a.getId() == null) throw new BusinessException("ID 不能为空");
        if (mapper.selectById(a.getId()) == null) throw new BusinessException("考勤记录不存在");
        fillSnapshot(a);
        mapper.update(a);
    }

    @Override
    public void remove(Long id) {
        if (mapper.selectById(id) == null) throw new BusinessException("考勤记录不存在");
        mapper.logicDelete(id);
    }
}

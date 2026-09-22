package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.context.UserContext;
import com.hr.dto.HrEmployeeQueryDTO;
import com.hr.entity.HrEmployee;
import com.hr.entity.SysUser;
import com.hr.mapper.HrAttendanceMapper;
import com.hr.mapper.HrEmployeeMapper;
import com.hr.mapper.HrPayrollMapper;
import com.hr.mapper.SysUserMapper;
import com.hr.service.HrEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HrEmployeeServiceImpl implements HrEmployeeService {

    private final HrEmployeeMapper mapper;
    private final SysUserMapper sysUserMapper;
    private final HrAttendanceMapper attendanceMapper;
    private final HrPayrollMapper payrollMapper;

    public HrEmployeeServiceImpl(HrEmployeeMapper mapper,
                                 SysUserMapper sysUserMapper,
                                 HrAttendanceMapper attendanceMapper,
                                 HrPayrollMapper payrollMapper) {
        this.mapper = mapper;
        this.sysUserMapper = sysUserMapper;
        this.attendanceMapper = attendanceMapper;
        this.payrollMapper = payrollMapper;
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
        // 行级数据权限：普通员工只能看自己（hr_employee 身份即主键 id）
        Long selfId = selfEmployeeId();
        if (selfId != null && !selfId.equals(e.getId())) {
            throw new BusinessException("无权查看该员工档案");
        }
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
        HrEmployee old = mapper.selectById(e.getId());
        if (old == null) throw new BusinessException("员工不存在");
        mapper.update(e);
        // 姓名快照级联刷新：员工改名后，同步更新其历史考勤/工资单记录中的姓名，
        // 避免「员工管理改了名、考勤/薪酬页仍显示旧名」的不一致（全量同步策略）
        if (old.getName() != null && !old.getName().equals(e.getName())) {
            attendanceMapper.updateEmpNameByEmployeeId(e.getId(), e.getName());
            payrollMapper.updateEmpNameByEmployeeId(e.getId(), e.getName());
        }
    }

    @Override
    public void remove(Long id) {
        if (mapper.selectById(id) == null) throw new BusinessException("员工不存在");
        mapper.logicDelete(id);
    }
}

package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.context.UserContext;
import com.hr.dto.HrPayrollQueryDTO;
import com.hr.entity.HrEmployee;
import com.hr.entity.HrPayroll;
import com.hr.entity.HrSalaryStandard;
import com.hr.entity.SysUser;
import com.hr.mapper.HrEmployeeMapper;
import com.hr.mapper.HrPayrollMapper;
import com.hr.mapper.HrSalaryStandardMapper;
import com.hr.mapper.SysUserMapper;
import com.hr.service.PayrollService;
import com.hr.vo.AttendanceSummaryVO;
import com.hr.vo.PayrollSourceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
public class PayrollServiceImpl implements PayrollService {

    private final HrPayrollMapper payrollMapper;
    private final HrEmployeeMapper employeeMapper;
    private final HrSalaryStandardMapper salaryStandardMapper;
    private final SysUserMapper sysUserMapper;

    public PayrollServiceImpl(HrPayrollMapper payrollMapper,
                              HrEmployeeMapper employeeMapper,
                              HrSalaryStandardMapper salaryStandardMapper,
                              SysUserMapper sysUserMapper) {
        this.payrollMapper = payrollMapper;
        this.employeeMapper = employeeMapper;
        this.salaryStandardMapper = salaryStandardMapper;
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
    public PageResult<HrPayroll> page(HrPayrollQueryDTO q) {
        // 员工只能看自己的工资单（薪酬保密）
        Long selfId = selfEmployeeId();
        if (selfId != null) q.setEmployeeId(selfId);
        int size = q.getSize() == null ? 10 : q.getSize();
        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int offset = (page - 1) * size;
        var list = payrollMapper.selectPage(q, offset);
        long total = payrollMapper.selectCount(q);
        return PageResult.of(total, list);
    }

    @Override
    public HrPayroll detail(Long id) {
        HrPayroll p = payrollMapper.selectById(id);
        if (p == null) throw new BusinessException("工资单不存在");
        return p;
    }

    @Override
    public void remove(Long id) {
        if (payrollMapper.selectById(id) == null) throw new BusinessException("工资单不存在");
        payrollMapper.logicDelete(id);
    }

    @Override
    public void generate(String salaryMonth) {
        // 1. 取所有在职员工（status=2）
        List<HrEmployee> employees = employeeMapper.selectByStatus(2); // 需在 HrEmployeeMapper 加此方法
        for (HrEmployee emp : employees) {
            HrSalaryStandard std = salaryStandardMapper.selectByEmployeeId(emp.getId());
            if (std == null) continue; // 没建薪资档案的跳过

            PayrollSourceVO src = payrollMapper.selectSource(emp.getId());
            AttendanceSummaryVO att = payrollMapper.selectAttendanceSummary(emp.getId(), salaryMonth);

            BigDecimal base   = nvl(std.getBaseSalary());
            BigDecimal post   = nvl(std.getPostSalary());
            BigDecimal perf   = nvl(std.getPerfSalary());
            // 日薪 = (基本+岗位) / 21.75
            BigDecimal daily  = base.add(post).divide(BigDecimal.valueOf(21.75), 2, RoundingMode.HALF_UP);

            // 加班费 = 日薪 * 1.5 * 加班小时
            BigDecimal overtimePay = daily.multiply(BigDecimal.valueOf(1.5))
                    .multiply(nvl(att.getOvertimeHours())).setScale(2, RoundingMode.HALF_UP);

            // 考勤扣款：迟到/早退每次 50；旷工扣 2 倍日薪；事假每天扣日薪；病假每天扣 0.5 日薪
            BigDecimal deduct = BigDecimal.ZERO;
            deduct = deduct.add(daily.multiply(BigDecimal.valueOf(2))
                    .multiply(BigDecimal.valueOf(att.getAbsentCount() == null ? 0 : att.getAbsentCount())));
            deduct = deduct.add(daily.multiply(BigDecimal.valueOf(1))
                    .multiply(BigDecimal.valueOf(att.getShijiaCount() == null ? 0 : att.getShijiaCount())));
            deduct = deduct.add(daily.multiply(BigDecimal.valueOf(0.5))
                    .multiply(BigDecimal.valueOf(att.getBingjiaCount() == null ? 0 : att.getBingjiaCount())));
            deduct = deduct.add(BigDecimal.valueOf(50)
                    .multiply(BigDecimal.valueOf((att.getLateCount() == null ? 0 : att.getLateCount())
                            + (att.getEarlyCount() == null ? 0 : att.getEarlyCount()))));

            // 应发 = 基本 + 岗位 + 绩效 + 加班费 - 考勤扣款
            BigDecimal gross = base.add(post).add(perf).add(overtimePay).subtract(deduct);

            // 社保个人 10.5%，公积金个人 7%（演示费率，可配）
            BigDecimal social = nvl(std.getSocialBase()).multiply(BigDecimal.valueOf(0.105)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal fund   = nvl(std.getFundBase()).multiply(BigDecimal.valueOf(0.07)).setScale(2, RoundingMode.HALF_UP);

            // 个税（简化累进）：起征 5000，超出部分 3%/10%/20%
            BigDecimal taxable = gross.subtract(social).subtract(fund).subtract(BigDecimal.valueOf(5000));
            BigDecimal tax = BigDecimal.ZERO;
            if (taxable.compareTo(BigDecimal.ZERO) > 0) {
                if (taxable.compareTo(BigDecimal.valueOf(3000)) <= 0)
                    tax = taxable.multiply(BigDecimal.valueOf(0.03));
                else if (taxable.compareTo(BigDecimal.valueOf(12000)) <= 0)
                    tax = taxable.multiply(BigDecimal.valueOf(0.10));
                else
                    tax = taxable.multiply(BigDecimal.valueOf(0.20));
                tax = tax.setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal net = gross.subtract(social).subtract(fund).subtract(tax).setScale(2, RoundingMode.HALF_UP);

            // 组装工资单（带快照）
            HrPayroll p = new HrPayroll();
            p.setEmployeeId(emp.getId());
            p.setSalaryMonth(salaryMonth);
            p.setEmpName(src != null ? src.getEmpName() : emp.getName());
            p.setDeptName(src != null ? src.getDeptName() : "");
            p.setPostName(src != null ? src.getPostName() : "");
            p.setBaseSalary(base); p.setPostSalary(post); p.setPerfSalary(perf);
            p.setOvertimePay(overtimePay); p.setAttendanceDeduct(deduct); p.setGrossPay(gross);
            p.setSocialPersonal(social); p.setFundPersonal(fund); p.setTax(tax); p.setNetPay(net);

            // 幂等：同员工同月份，存在则更新，不存在则插入
            HrPayroll old = payrollMapper.selectByEmpMonth(emp.getId(), salaryMonth);
            if (old == null) payrollMapper.insert(p);
            else { p.setId(old.getId()); payrollMapper.update(p); }
        }
    }
    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}


package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.dto.HrDepartmentQueryDTO;
import com.hr.entity.HrDepartment;
import com.hr.mapper.HrDepartmentMapper;
import com.hr.service.HrDepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class HrDepartmentServiceImpl implements HrDepartmentService {

    private final HrDepartmentMapper mapper;
    public HrDepartmentServiceImpl(HrDepartmentMapper mapper) { this.mapper = mapper; }

    @Override
    public PageResult<HrDepartment> page(HrDepartmentQueryDTO q) {
        int size = q.getSize() == null ? 10 : q.getSize();
        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int offset = (page - 1) * size;
        var list = mapper.selectPage(q, offset);
        long total = mapper.selectCount(q);
        return PageResult.of(total, list);
    }

    @Override
    public List<HrDepartment> listAll() { return mapper.selectAll(); }

    @Override
    public HrDepartment detail(Long id) {
        HrDepartment d = mapper.selectById(id);
        if (d == null) throw new BusinessException("部门不存在");
        return d;
    }

    @Override
    public void add(HrDepartment d) {
        if (d.getStatus() == null) d.setStatus(1);
        if (d.getSort() == null) d.setSort(0);
        if (d.getParentId() == null) d.setParentId(0L);
        mapper.insert(d);
    }

    @Override
    public void update(HrDepartment d) {
        if (d.getId() == null) throw new BusinessException("ID 不能为空");
        if (mapper.selectById(d.getId()) == null) throw new BusinessException("部门不存在");
        mapper.update(d);
    }

    @Override
    public void remove(Long id) {
        if (mapper.selectById(id) == null) throw new BusinessException("部门不存在");
        mapper.logicDelete(id);
    }
}


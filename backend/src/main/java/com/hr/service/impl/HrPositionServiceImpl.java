package com.hr.service.impl;
import com.hr.common.exception.BusinessException;
import com.hr.common.result.PageResult;
import com.hr.dto.HrPositionQueryDTO;
import com.hr.entity.HrPosition;
import com.hr.mapper.HrPositionMapper;
import com.hr.service.HrPositionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HrPositionServiceImpl implements HrPositionService {

    private final HrPositionMapper mapper;
    public HrPositionServiceImpl(HrPositionMapper mapper) { this.mapper = mapper; }

    @Override
    public PageResult<HrPosition> page(HrPositionQueryDTO q) {
        int size = q.getSize() == null ? 10 : q.getSize();
        int page = q.getPage() == null || q.getPage() < 1 ? 1 : q.getPage();
        int offset = (page - 1) * size;
        var list = mapper.selectPage(q, offset);
        long total = mapper.selectCount(q);
        return PageResult.of(total, list);
    }

    @Override
    public HrPosition detail(Long id) {
        HrPosition p = mapper.selectById(id);
        if (p == null) throw new BusinessException("岗位不存在");
        return p;
    }

    @Override
    public void add(HrPosition p) {
        if (p.getStatus() == null) p.setStatus(1);
        if (p.getHeadcount() == null) p.setHeadcount(0);
        mapper.insert(p);
    }

    @Override
    public void update(HrPosition p) {
        if (p.getId() == null) throw new BusinessException("ID 不能为空");
        if (mapper.selectById(p.getId()) == null) throw new BusinessException("岗位不存在");
        mapper.update(p);
    }

    @Override
    public void remove(Long id) {
        if (mapper.selectById(id) == null) throw new BusinessException("岗位不存在");
        mapper.logicDelete(id);
    }
}
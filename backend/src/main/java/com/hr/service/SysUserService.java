package com.hr.service;
import com.hr.common.result.PageResult;
import com.hr.dto.SysUserQueryDTO;
import com.hr.dto.SysUserSaveDTO;
import com.hr.vo.SysUserVO;

import java.util.List;

public interface SysUserService {
    PageResult<SysUserVO> page(SysUserQueryDTO q);
    SysUserVO detail(Long id);
    void add(SysUserSaveDTO dto);
    void update(SysUserSaveDTO dto);
    void remove(Long id);
    /** 重置密码为默认 123456（BCrypt 加密后入库） */
    void resetPassword(Long id);
}

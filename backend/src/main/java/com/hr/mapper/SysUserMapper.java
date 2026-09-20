package com.hr.mapper;

import com.hr.dto.SysUserQueryDTO;
import com.hr.entity.SysMenu;
import com.hr.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysUserMapper {

    /* ===== 阶段④已有（登录/鉴权用，保持不变） ===== */
    SysUser selectByUsername(@Param("username") String username);
    SysUser selectById(@Param("id") Long id);
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);
    List<String> selectPermsByUserId(@Param("userId") Long userId);
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    int updateLastLoginTime(@Param("id") Long id, @Param("time") LocalDateTime time);
    int insert(SysUser user);
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
    Long selectRoleIdByKey(@Param("roleKey") String roleKey);
    long countAll();

    /* ===== 系统管理模块新增 ===== */
    /** 用户分页查询（is_deleted=0） */
    List<SysUser> selectUserPage(@Param("q") SysUserQueryDTO q, @Param("offset") int offset);
    /** 用户分页总数 */
    long countUser(@Param("q") SysUserQueryDTO q);
    /** 校验用户名是否已被其他用户占用（新增/改名时用） */
    long countUsername(@Param("username") String username, @Param("exceptId") Long exceptId);
    /** 逻辑删除用户 */
    int logicDeleteUser(@Param("id") Long id);
    /** 删除某用户的全部角色绑定（重新授权前先清空） */
    int deleteUserRole(@Param("userId") Long userId);
    /** 查某用户绑定的角色 id 列表 */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    /** 重置密码：把密码更新为指定哈希 */
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    /** 更新非密码字段 */
    int updateUser(SysUser user);
    /** 行级权限：根据登录用户 id 查出其关联的员工档案 id（sys_user.employee_id） */
    Long selectEmployeeIdByUserId(@Param("userId") Long userId);
}

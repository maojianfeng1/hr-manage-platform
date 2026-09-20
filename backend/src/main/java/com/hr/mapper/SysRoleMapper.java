package com.hr.mapper;
import com.hr.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    /** 查全部角色（角色数量少，不分页，一次返回） */
    List<SysRole> selectAll();
    SysRole selectById(@Param("id") Long id);
    /** 角色标识唯一性校验（新增/改名时用） */
    long countByKey(@Param("roleKey") String roleKey, @Param("exceptId") Long exceptId);
    int insert(SysRole role);
    int update(SysRole role);
    int logicDelete(@Param("id") Long id);

    /** 角色-菜单绑定（授权） */
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
    int deleteRoleMenu(@Param("roleId") Long roleId);
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /** 角色被删时，清空用户-角色绑定 */
    int deleteUserRoleByRoleId(@Param("roleId") Long roleId);
}

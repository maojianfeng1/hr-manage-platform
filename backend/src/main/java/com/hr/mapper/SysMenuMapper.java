package com.hr.mapper;
import com.hr.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 查全部菜单（含按钮权限点），用于「角色授权」时展示完整菜单树。
 * 注意：这里不分用户，返回的是系统里所有菜单，只有管理员配角色时才用得到。
 */
@Mapper
public interface SysMenuMapper {
    List<SysMenu> selectAll();

    /** 新增菜单 */
    int insert(SysMenu menu);

    /** 按主键更新菜单 */
    int updateById(SysMenu menu);

    /** 逻辑删除菜单 */
    int deleteById(Long id);

    /** 根据主键查询 */
    SysMenu selectById(Long id);

    /** 统计某父菜单下的子节点数（删除前校验） */
    int countByParentId(Long parentId);
}

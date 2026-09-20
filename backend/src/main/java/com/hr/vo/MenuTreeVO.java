package com.hr.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单树节点（前端侧边栏渲染用）
 *
 * 知识点：数据库的菜单是「扁平表」（每行一个菜单，parent_id 指向父级），
 * 但前端 Element Plus 的 el-menu 需要「树形结构」（children 嵌套 children）。
 * 所以后端要把扁平列表转成树，这个转换放在 Service 层完成，Controller 只负责返回。
 */
@Data
public class MenuTreeVO implements Serializable {

    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String icon;
    private String perms;
    private Integer sort;
    private List<MenuTreeVO> children;
}

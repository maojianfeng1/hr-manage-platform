package com.hr.service.impl;
import com.hr.entity.SysMenu;
import com.hr.mapper.SysMenuMapper;
import com.hr.mapper.SysUserMapper;
import com.hr.service.MenuService;
import com.hr.vo.MenuTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;

    public MenuServiceImpl(SysUserMapper userMapper, SysMenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public List<MenuTreeVO> getUserMenus(Long userId) {
        List<SysMenu> flat = userMapper.selectMenusByUserId(userId);
        List<MenuTreeVO> all = flat.stream().map(this::toVO).collect(Collectors.toList());
        return buildTree(all);
    }

    @Override
    public List<MenuTreeVO> getAllMenusTree() {
        List<SysMenu> flat = menuMapper.selectAll();
        List<MenuTreeVO> all = flat.stream().map(this::toVO).collect(Collectors.toList());
        return buildTree(all);
    }

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<SysMenu> flat = menuMapper.selectAll();
        List<MenuTreeVO> all = flat.stream().map(this::toVO).collect(Collectors.toList());
        return buildTree(all);
    }

    @Override
    public void saveMenu(SysMenu menu) {
        if (menu.getMenuType() == null) {
            menu.setMenuType(1);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        menuMapper.insert(menu);
    }

    @Override
    public void updateMenu(SysMenu menu) {
        menuMapper.updateById(menu);
    }

    @Override
    public void removeMenu(Long id) {
        int children = menuMapper.countByParentId(id);
        if (children > 0) {
            throw new RuntimeException("该菜单下存在子菜单，请先删除子菜单");
        }
        menuMapper.deleteById(id);
    }

    private MenuTreeVO toVO(SysMenu m) {
        MenuTreeVO v = new MenuTreeVO();
        v.setId(m.getId());
        v.setParentId(m.getParentId());
        v.setMenuName(m.getMenuName());
        v.setMenuType(m.getMenuType());
        v.setPath(m.getPath());
        v.setComponent(m.getComponent());
        v.setIcon(m.getIcon());
        v.setPerms(m.getPerms());
        v.setSort(m.getSort());
        return v;
    }

    /** 扁平列表 → 树形结构（自关联表的经典转换，与阶段④完全一致） */
    private List<MenuTreeVO> buildTree(List<MenuTreeVO> all) {
        Map<Long, List<MenuTreeVO>> childrenMap = all.stream()
                .filter(m -> m.getParentId() != null && m.getParentId() != 0)
                .collect(Collectors.groupingBy(MenuTreeVO::getParentId));

        List<MenuTreeVO> roots = all.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());

        roots.sort(Comparator.comparingInt(m -> m.getSort() == null ? 0 : m.getSort()));
        roots.forEach(r -> r.setChildren(findChildren(r.getId(), childrenMap)));
        return roots;
    }

    private List<MenuTreeVO> findChildren(Long parentId, Map<Long, List<MenuTreeVO>> childrenMap) {
        List<MenuTreeVO> kids = childrenMap.get(parentId);
        if (kids == null) {
            return new ArrayList<>();
        }
        kids.sort(Comparator.comparingInt(m -> m.getSort() == null ? 0 : m.getSort()));
        kids.forEach(k -> k.setChildren(findChildren(k.getId(), childrenMap)));
        return kids;
    }
}

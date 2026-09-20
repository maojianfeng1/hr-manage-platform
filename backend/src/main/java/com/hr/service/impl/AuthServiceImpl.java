package com.hr.service.impl;

import com.hr.common.exception.BusinessException;
import com.hr.dto.LoginDTO;
import com.hr.entity.SysUser;
import com.hr.mapper.SysLoginLogMapper;
import com.hr.mapper.SysUserMapper;
import com.hr.service.AuthService;
import com.hr.util.JwtUtil;
import com.hr.vo.LoginVO;
import com.hr.vo.UserInfoVO;
import com.hr.vo.UserProfileVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现
 *
 * 这是整个登录链路的「业务心脏」。把登录这一步的所有规则集中在这里，
 * Controller 只负责把 HTTP 请求转成方法调用、把返回值包成 Result。
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 构造器注入：Spring 推荐用构造器注入依赖，配合 final 字段保证不可变、必填
    public AuthServiceImpl(SysUserMapper userMapper,
                           SysLoginLogMapper loginLogMapper,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.loginLogMapper = loginLogMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVO login(LoginDTO dto, String ip) {
        // 1. 查用户（is_deleted=0 的有效账号）
        SysUser user = userMapper.selectByUsername(dto.getUsername());

        // 2. 校验用户名 + 密码。注意：用户名不存在 和 密码错误 给「同一个」提示，
        //    绝不区分报 "用户不存在" 还是 "密码错误" —— 防止黑客枚举哪些账号存在。
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            saveLoginLog(dto.getUsername(), ip, 0, "用户名或密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 校验账号状态（1 启用 / 0 停用）
        if (user.getStatus() == null || user.getStatus() != 1) {
            saveLoginLog(dto.getUsername(), ip, 0, "账号已停用");
            throw new BusinessException("账号已被停用，请联系管理员");
        }

        // 4. 查出该用户的角色集合与权限集合
        List<String> roles = userMapper.selectRoleKeysByUserId(user.getId());
        List<String> perms = userMapper.selectPermsByUserId(user.getId());

        // 5. 签发 JWT（payload 里放 userId 和 username）
        String token = jwtUtil.createToken(user.getId(), user.getUsername());

        // 6. 更新最后登录时间（演示「登录即写入库」）
        userMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());

        // 7. 写登录成功日志
        saveLoginLog(dto.getUsername(), ip, 1, "登录成功");

        // 8. 组装返回给前端的 LoginVO
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(jwtUtil.getExpireSeconds());
        UserInfoVO info = new UserInfoVO();
        BeanUtils.copyProperties(user, info);     // 只拷贝同名同类型字段
        info.setUserId(user.getId());
        vo.setUserInfo(info);
        vo.setRoles(roles);
        vo.setPerms(perms);
        return vo;
    }

    @Override
    public UserProfileVO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 刷新页面后只靠 token 拉资料，必须一次性返回「基本信息 + 角色 + 权限」
        List<String> roles = userMapper.selectRoleKeysByUserId(userId);
        List<String> perms = userMapper.selectPermsByUserId(userId);
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setEmployeeId(user.getEmployeeId());
        vo.setRoles(roles);
        vo.setPerms(perms);
        return vo;
    }

    /** 登录日志写入（失败/成功都记） */
    private void saveLoginLog(String username, String ip, int status, String msg) {
        try {
            loginLogMapper.insert(username, ip, status, msg);
        } catch (Exception e) {
            // 日志写失败绝不能影响登录主流程，吞掉异常只打印
            log.warn("写入登录日志失败：{}", e.getMessage());
        }
    }
}

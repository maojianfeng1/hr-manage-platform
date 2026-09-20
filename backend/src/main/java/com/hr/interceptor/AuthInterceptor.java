package com.hr.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hr.annotation.RequiresPermission;
import com.hr.common.result.Result;
import com.hr.common.result.ResultCode;
import com.hr.context.UserContext;
import com.hr.mapper.SysUserMapper;
import com.hr.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 认证拦截器：所有 /api/** 请求到达 Controller 之前先过这里。
 *
 * 流程：取 token → 验签/过期 → 查当前用户角色+权限 → 注入 UserContext
 *      → 若方法带 @RequiresPermission 则校验权限 → 不通过直接 401/403。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtil jwtUtil, SysUserMapper userMapper, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 只对「处理方法」做鉴权（放行静态资源等无 handler 的请求）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 2. 取 token（前端格式：Authorization: Bearer xxx）
        String header = request.getHeader(jwtUtil.getHeader());
        String prefix = jwtUtil.getPrefix().trim();
        if (header == null || !header.startsWith(prefix)) {
            writeJson(response, ResultCode.UNAUTHORIZED, "未登录或登录状态已过期");
            return false;
        }
        String token = header.substring(prefix.length()).trim();

        // 3. 解析 token（过期/篡改都会抛 JwtException）
        Long userId;
        String username;
        try {
            userId = jwtUtil.getUserId(token);
            username = jwtUtil.parse(token).get("username", String.class);
        } catch (JwtException e) {
            writeJson(response, ResultCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
            return false;
        }

        // 4. 查当前用户的角色 + 权限集合（RBAC：权限来自数据库，不写死）
        List<String> roles = userMapper.selectRoleKeysByUserId(userId);
        List<String> perms = userMapper.selectPermsByUserId(userId);
        Long empId = userMapper.selectEmployeeIdByUserId(userId);
        UserContext.UserInfo info = new UserContext.UserInfo();
        info.setUserId(userId);
        info.setUsername(username);
        info.setRoles(roles);
        info.setPerms(perms);
        info.setEmpId(empId);
        UserContext.set(info);

        // 5. 方法级权限注解校验
        HandlerMethod hm = (HandlerMethod) handler;
        RequiresPermission annotation = hm.getMethodAnnotation(RequiresPermission.class);
        if (annotation != null) {
            String need = annotation.value();
            if (perms == null || !perms.contains(need)) {
                UserContext.clear();
                writeJson(response, ResultCode.FORBIDDEN, "没有操作权限");
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束务必清理，防止线程复用导致用户串号
        UserContext.clear();
    }

    /** 直接往响应写 Result JSON（HTTP 200 + body.code=401/403，前端拦截器据此跳转/提示） */
    private void writeJson(HttpServletResponse response, ResultCode code, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code)));
    }
}

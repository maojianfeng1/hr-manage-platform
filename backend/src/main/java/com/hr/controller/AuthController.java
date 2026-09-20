package com.hr.controller;

import com.hr.common.result.Result;
import com.hr.common.result.ResultCode;
import com.hr.context.UserContext;
import com.hr.dto.LoginDTO;
import com.hr.service.AuthService;
import com.hr.vo.LoginVO;
import com.hr.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器（登录入口）
 *
 * 知识点：路径约定
 * - 所有后端接口统一以 /api 开头（前端 Vite proxy 只转发 /api，避免把静态资源也代理走）
 * - /api/auth/login 是「免登录白名单」里的方法（阶段④-2 的拦截器会放行它）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        LoginVO vo = authService.login(dto, getClientIp(request));
        return Result.success("登录成功", vo);
    }

    /**
     * 获取当前登录用户信息。
     * 用于页面刷新后前端重新拉取用户资料（token 还在，但内存里的 userInfo 没了）。
     * 走到这里说明拦截器已校验过 token，直接从 UserContext 取 userId 即可。
     */
    @GetMapping("/info")
    public Result<UserProfileVO> info() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        return Result.success(authService.getUserInfo(userId));
    }

    /** 退出登录：清理当前线程的用户上下文（JWT 本身无状态，前端负责删 token） */
    @PostMapping("/logout")
    public Result<?> logout() {
        UserContext.clear();
        return Result.success("退出成功");
    }

    /** 取客户端真实 IP（考虑反向代理场景，优先取 X-Forwarded-For 的第一个） */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

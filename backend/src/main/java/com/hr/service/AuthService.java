package com.hr.service;

import com.hr.dto.LoginDTO;
import com.hr.vo.LoginVO;
import com.hr.vo.UserProfileVO;

/**
 * 认证服务接口
 *
 * 知识点：为什么 Service 层要写接口 + 实现类两套，而不是直接在 Controller 里写逻辑？
 * 1. 解耦：Controller 只管「接收请求、返回结果」，业务规则都藏在 Service 里。
 * 2. 可测试：单元测试可以 new 一个假的实现（Mock）来测 Controller，不用连真数据库。
 * 3. 可替换：将来换实现（比如登录逻辑从「账号密码」改成「手机号验证码」），
 *    只要再写一个 Impl，Controller 一行不用动。
 */
public interface AuthService {

    /**
     * 登录
     * @param dto 用户名 + 密码
     * @param ip  调用方 IP（用于写登录日志）
     * @return 登录成功后的令牌与用户信息
     */
    LoginVO login(LoginDTO dto, String ip);

    /**
     * 获取当前登录用户的详细信息（供页面刷新后重新拉取，避免依赖前端内存中的临时状态）
     * @param userId 当前登录用户 id
     */
    UserProfileVO getUserInfo(Long userId);
}

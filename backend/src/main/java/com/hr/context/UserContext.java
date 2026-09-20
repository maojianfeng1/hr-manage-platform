package com.hr.context;

import lombok.Data;

import java.util.List;

/**
 * 登录用户上下文：用 ThreadLocal 在「一次请求」内传递当前用户信息。
 *
 * 为什么用 ThreadLocal？
 * - Web 服务器对每次请求开一个线程。ThreadLocal 让这个线程在 Controller/Service 任何地方
 *   都能拿到当前登录用户，而不用把 userId 从 Controller 一层层传参到 Service（参数透传污染）。
 * - 必须在请求结束时 clear()，否则线程池复用会导致「用户串号」（A 的请求读到 B 的数据）。
 *   清除动作放在 AuthInterceptor.afterCompletion 里做。
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> HOLDER = new ThreadLocal<>();

    @Data
    public static class UserInfo {
        private Long userId;
        private String username;
        private List<String> roles;
        private List<String> perms;
        /** 当前登录账号关联的员工档案 id（行级数据权限用：只能看自己的考勤/工资） */
        private Long empId;
    }

    public static void set(UserInfo info) { HOLDER.set(info); }

    public static UserInfo get() { return HOLDER.get(); }

    public static Long getUserId() {
        return HOLDER.get() == null ? null : HOLDER.get().userId;
    }

    public static void clear() { HOLDER.remove(); }
}

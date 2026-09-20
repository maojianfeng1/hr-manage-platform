package com.hr.init;

import com.hr.entity.SysUser;
import com.hr.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据初始化器（启动即执行一次）
 *
 * 为什么用 CommandLineRunner 而不是把账号写进建表 SQL？
 * 1. SQL 里无法直接用 BCrypt 算出哈希（那是 Java 代码干的事），手算哈希既麻烦又易错，
 *    一旦和代码里的算法/版本对不上，就会出现「账号对了却登不进」的低级 bug。
 * 2. 把「创建账号」交给代码，保证「库里存的哈希」和「登录时校验用的算法」100% 同源。
 *
 * 为什么只在「库里没有用户时」才初始化（countAll==0 判断）？
 * 避免每次重启都重复插入、或把你在阶段⑦手改过的账号覆盖掉。幂等保护。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 三个演示账号，密码统一 123456，分别绑定 admin / hr / employee 三个角色 */
    private static final String[][] ACCOUNTS = {
            {"admin", "系统管理员", "admin"},
            {"hr", "人事专员", "hr"},
            {"employee", "普通员工", "employee"}
    };

    @Override
    @Transactional   // 一次启动要么全成功要么回滚，避免插了用户却没绑角色
    public void run(String... args) {
        if (userMapper.countAll() > 0) {
            log.info("[初始化] 检测到已存在用户，跳过账号初始化");
            return;
        }
        for (String[] acc : ACCOUNTS) {
            createAccount(acc[0], acc[1], acc[2]);
        }
        log.info("[初始化] 已创建 3 个演示账号：admin / hr / employee（密码均为 123456）");
    }

    private void createAccount(String username, String realName, String roleKey) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("123456"));   // 关键：密码加密后入库
        user.setRealName(realName);
        user.setStatus(1);
        userMapper.insert(user);                              // useGeneratedKeys 回写 id

        Long roleId = userMapper.selectRoleIdByKey(roleKey);
        if (roleId != null) {
            userMapper.insertUserRole(user.getId(), roleId);
        }
        log.info("[初始化] 账号 {} / 123456（角色 {}）创建完成", username, roleKey);
    }
}

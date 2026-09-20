package com.hr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置
 *
 * 知识点：为什么密码绝不能明文存、也不能简单 MD5？
 *
 * 1. 明文存 = 数据库一旦泄露，所有用户密码直接暴露，且用户往往多站复用同一密码。
 *
 * 2. 单纯 MD5/SHA1 也不够：这类算法「太快」，配合彩虹表或 GPU 每秒上亿次爆破，
 *    8 位以内的弱密码基本秒破。
 *
 * 3. BCrypt 的两个关键设计正好治这两个病：
 *    - 「加盐」：每次 encode 出来的哈希都不同（盐随机生成并混在结果字符串里），
 *      所以即使两个人密码都是 123456，库里两行哈希也完全不一样 → 彩虹表失效。
 *    - 「可调慢」：自带工作量因子（默认 strength=10，即 2^10 次迭代），
 *      单次校验约几十毫秒。对正常登录无感，但把爆破成本抬高几个数量级。
 *
 * 4. 校验方式：不是「解密后比对」（BCrypt 不可逆），而是
 *    passwordEncoder.matches(明文, 库里的哈希) —— 内部用同样参数重新算一遍比对。
 *
 * 5. 为什么只引 spring-security-crypto 而不用 spring-boot-starter-security？
 *    后者会带来整套过滤器链和默认登录页，对「自己写拦截器做鉴权」的教学项目是干扰。
 *    只引 crypto 模块，等价于「只要这个加密工具」，零侵入。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

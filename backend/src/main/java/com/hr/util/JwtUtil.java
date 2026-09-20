package com.hr.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：负责「签发令牌」和「解析令牌」两件事。
 *
 * 知识点：
 * 1. JWT 是什么？
 *    一串字符串，分三段（header.payload.signature），base64 编码、用密钥签名。
 *    它「自包含」：把 userId、username 这些信息直接放 payload 里，后端拿到令牌就能解出
 *    当前用户是谁，不用每次查库、不用存 session —— 这就是它「无状态」的好处。
 *
 * 2. 为什么用 HS256 对称加密（一个密钥既签名又验签）？
 *    本项目是单体服务，一个密钥足够。如果是多服务且要「资源服务不持有密钥也能验签」，
 *    才考虑 RS256 非对称（公钥验、私钥签）。教学项目用 HS256 最直观。
 *
 * 3. 安全红线：
 *    - 密钥必须足够长（HS256 要求 ≥256 bit = 32 字节），否则 jjwt 直接报错。
 *    - payload 里只能放「非敏感」信息（id、用户名），绝不能放密码、余额。
 *    - JWT 一旦签发无法「主动作废」，所以有效期要短（本项目 2 小时），
 *      真正需要「强制下线」得额外维护黑名单（阶段④-2 的 Token 黑名单会讲）。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireSeconds;
    private final String header;
    private final String prefix;

    public JwtUtil(@Value("${hr.jwt.secret}") String secret,
                   @Value("${hr.jwt.expire-seconds}") long expireSeconds,
                   @Value("${hr.jwt.header}") String header,
                   @Value("${hr.jwt.prefix}") String prefix) {
        // Keys.hmacShaKeyFor 会自动校验密钥长度是否 ≥ 32 字节
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
        this.header = header;
        this.prefix = prefix;
    }

    /** 签发令牌：把 userId 作为 subject，username 放 claim */
    public String createToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key)            // HS256，使用构造时生成的密钥
                .compact();
    }

    /** 解析令牌：验签 + 提取 payload。签名失败 / 过期都会抛异常，由拦截器统一处理 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从令牌取出用户 id（subject 存的就是 userId） */
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public String getHeader() {
        return header;
    }

    public String getPrefix() {
        return prefix;
    }
}

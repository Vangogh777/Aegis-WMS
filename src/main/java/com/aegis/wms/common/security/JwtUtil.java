package com.aegis.wms.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    // 密钥(实际项目中应从配置文件读取)
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token有效期: 24小时
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000L;

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username, String roleType) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roleType", roleType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从Token获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从Token获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从Token获取角色类型
     */
    public String getRoleType(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("roleType", String.class) : null;
    }
}
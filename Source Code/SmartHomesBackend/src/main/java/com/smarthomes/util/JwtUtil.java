package com.smarthomes.util;

import com.smarthomes.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public static User verifyToken(String token) {
        try {
            Claims claims = validateToken(token);
            User user = new User();
            user.setId(claims.get("id", Integer.class));
            user.setEmail(claims.getSubject());
            user.setRole(User.Role.valueOf(claims.get("role", String.class)));
            return user;
        } catch (Exception e) {
            return null;
        }
    }

}
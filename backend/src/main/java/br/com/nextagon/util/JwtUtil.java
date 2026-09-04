package br.com.nextagon.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final int MINIMUM_SECRET_LENGTH = 32;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private Key getSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET não foi configurado"
            );
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        if (secretBytes.length < MINIMUM_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET deve possuir pelo menos 32 bytes"
            );
        }

        return Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(
            String userId,
            String email,
            String role
    ) {
        return buildToken(userId, email, role, expiration);
    }

    public String generateRefreshToken(
            String userId,
            String email,
            String role
    ) {
        return buildToken(userId, email, role, refreshExpiration);
    }

    private String buildToken(
            String userId,
            String email,
            String role,
            long ttl
    ) {
        if (ttl <= 0) {
            throw new IllegalStateException(
                    "Tempo de expiração do JWT deve ser maior que zero"
            );
        }

        return Jwts.builder()
                .setSubject(userId)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + ttl)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token)
                .getBody()
                .get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Token JWT não pode ser vazio"
            );
        }

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }
}
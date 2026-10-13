package com.nextlevel.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String DEFAULT_DEMO_SECRET =
            "nextlevel-store-demo-jwt-secret-change-in-render-2026-very-long-key";
    private static final long DEFAULT_EXPIRATION_MILLIS = 86_400_000L;

    private final SecretKey secretKey;
    private final long expirationMillis;

    public JwtService(@Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMillis) {
        String normalizedSecret = normalizeSecret(secret);
        this.secretKey = Keys.hmacShaKeyFor(sha256(normalizedSecret));
        this.expirationMillis = expirationMillis > 0 ? expirationMillis : DEFAULT_EXPIRATION_MILLIS;
    }

    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String normalizeSecret(String secret) {
        if (secret == null) {
            return DEFAULT_DEMO_SECRET;
        }

        String trimmed = secret.trim();
        if (trimmed.isBlank()
                || "value".equalsIgnoreCase(trimmed)
                || trimmed.toLowerCase().contains("your_jwt_secret")
                || trimmed.toLowerCase().contains("tu_jwt_secret")) {
            return DEFAULT_DEMO_SECRET;
        }

        return trimmed;
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No fue posible preparar la clave JWT.", ex);
        }
    }
}

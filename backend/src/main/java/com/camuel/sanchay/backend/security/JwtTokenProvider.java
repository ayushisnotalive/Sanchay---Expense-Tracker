package com.camuel.sanchay.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${sanchay.jwt.secret:5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437}") String secret,
            @Value("${sanchay.jwt.expiration-ms:86400000}") long jwtExpirationMs,
            @Value("${sanchay.jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        
        SecretKey secretKey;
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        this.key = secretKey;
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId(), userPrincipal.getUsername(), jwtExpirationMs);
    }

    public String generateRefreshToken(Long userId, String username) {
        return generateTokenFromUserId(userId, username, refreshExpirationMs);
    }

    public String generateTokenFromUserId(Long userId, String username, long expiryMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("Invalid JWT token: {}", ex.getMessage());
        }
        return false;
    }
}

package ru.mtuci.rbpo.lab2.service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final JwtParser parser;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        parser = Jwts.parser().verifyWith(key).build();
    }

    public String createAccessToken(String username, Instant issuedAt, Instant expiresAt) {
        return createToken(username, "access", issuedAt, expiresAt);
    }

    public String createRefreshToken(String username, Instant issuedAt, Instant expiresAt) {
        return createToken(username, "refresh", issuedAt, expiresAt);
    }

    public Claims checkAccessToken(String token) {
        return checkToken(token, "access");
    }

    public Claims checkRefreshToken(String token) {
        return checkToken(token, "refresh");
    }

    private String createToken(String username, String type, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("type", type)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private Claims checkToken(String token, String type) {
        var signed = parser.parseSignedClaims(token);
        Claims claims = signed.getPayload();
        if (!"HS256".equals(signed.getHeader().getAlgorithm())
                || !type.equals(claims.get("type", String.class))
                || claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getId() == null || claims.getId().isBlank()
                || claims.getIssuedAt() == null || claims.getExpiration() == null
                || claims.getIssuedAt().after(new Date())
                || !claims.getExpiration().after(claims.getIssuedAt())
                || !claims.getExpiration().toInstant().isAfter(Instant.now())) {
            throw new JwtException("Недействительный токен");
        }
        return claims;
    }
}

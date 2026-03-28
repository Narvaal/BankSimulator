package br.com.ale.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;

import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;


    public String generateToken(long clientId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(clientId))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignKey())
                .compact();
    }


    public long extractClientId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return Long.parseLong(subject);
    }

    public Instant extractExpiration(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.toInstant();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token, long expectedClientId) {
        try {
            long clientId = extractClientId(token);
            return clientId == expectedClientId && isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(Instant.now());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

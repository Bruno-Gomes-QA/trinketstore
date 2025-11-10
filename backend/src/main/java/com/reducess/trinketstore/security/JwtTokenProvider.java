package com.reducess.trinketstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.reducess.trinketstore.config.SupabaseConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final SupabaseConfig supabaseConfig;

    public Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(supabaseConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractAuthId(String token) {
        Claims claims = validateToken(token);
        String sub = claims.getSubject();
        return UUID.fromString(sub);
    }

    public String extractEmail(String token) {
        Claims claims = validateToken(token);
        return claims.get("email", String.class);
    }
}


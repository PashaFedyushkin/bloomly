package by.fedyushkin.bloomly.security;

import by.fedyushkin.bloomly.entity.Role;
import by.fedyushkin.bloomly.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    public record RefreshToken(String value, String jti) {
    }

    private static final String TOKEN_TYPE = "token_type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey secretKey;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(TOKEN_TYPE, ACCESS)
                .claim("phone", user.getPhone())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .signWith(secretKey)
                .compact();
    }

    public RefreshToken generateRefreshToken(User user) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        String value = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .id(jti)
                .claim(TOKEN_TYPE, REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .signWith(secretKey)
                .compact();
        return new RefreshToken(value, jti);
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return ACCESS.equals(claims.get(TOKEN_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return REFRESH.equals(claims.get(TOKEN_TYPE, String.class));
    }
}

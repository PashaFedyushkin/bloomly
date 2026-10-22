package by.fedyushkin.bloomly.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenStoreService {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RefreshTokenStoreService(
            StringRedisTemplate redis,
            @Value("${jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.redis = redis;
        this.ttl = Duration.ofSeconds(refreshTtlSeconds);
    }

    public void save(String jti, Long userId) {
        redis.opsForValue().set(KEY_PREFIX + jti, String.valueOf(userId), ttl);
    }

    public String consume(String jti) {
        return redis.opsForValue().getAndDelete(KEY_PREFIX + jti);
    }

    public void delete(String jti) {
        redis.delete(KEY_PREFIX + jti);
    }
}

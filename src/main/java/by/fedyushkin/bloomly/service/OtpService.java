package by.fedyushkin.bloomly.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String CODE_KEY_PREFIX = "auth:otp:code:";
    private static final String ATTEMPT_KEY_PREFIX = "auth:otp:attempts:";
    private static final String SENT_KEY_PREFIX = "auth:otp:sent:";

    private final StringRedisTemplate redis;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${auth.otp.ttl-seconds}")
    @Getter
    private long ttlSeconds;

    @Value("${auth.otp.length}")
    private int length;

    @Value("${auth.otp.resend-interval-seconds}")
    private long resendIntervalSeconds;

    @Value("${auth.otp.max-attempts}")
    private int maxAttempts;

    public String generateAndStore(String phone) {
        Boolean reserved = redis.opsForValue().setIfAbsent(
                SENT_KEY_PREFIX + phone,
                "1",
                Duration.ofSeconds(resendIntervalSeconds)
        );
        if (!Boolean.TRUE.equals(reserved)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Код уже отправлен. Повторите позже.");
        }
        String code = generateCode();
        Duration ttl = Duration.ofSeconds(ttlSeconds);
        redis.opsForValue().set(CODE_KEY_PREFIX + phone, sha256(code), ttl);
        redis.opsForValue().set(ATTEMPT_KEY_PREFIX + phone, "0", ttl);
        return code;
    }

    public void verify(String phone, String code) {
        String storedHash = redis.opsForValue().get(CODE_KEY_PREFIX + phone);
        if (storedHash == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный или просроченный код");
        }

        String attemptValue = redis.opsForValue().get(ATTEMPT_KEY_PREFIX + phone);
        int attempts = Integer.parseInt(attemptValue == null ? "0" : attemptValue);
        if (attempts >= maxAttempts) {
            invalidate(phone);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Превышено число попыток. Запросите код заново.");
        }

        if (!storedHash.equals(sha256(code))) {
            redis.opsForValue().increment(ATTEMPT_KEY_PREFIX + phone);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный или просроченный код");
        }

        invalidate(phone);
    }

    public void invalidate(String phone) {
        redis.delete(List.of(
                CODE_KEY_PREFIX + phone,
                ATTEMPT_KEY_PREFIX + phone,
                SENT_KEY_PREFIX + phone
        ));
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, length);
        return String.format("%0" + length + "d", secureRandom.nextInt(bound));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

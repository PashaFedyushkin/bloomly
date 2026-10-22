package by.fedyushkin.bloomly.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookie {

    public static final String NAME = "refresh_token";
    private static final String PATH = "/auth";

    private final boolean secure;
    private final Duration maxAge;

    public RefreshCookie(
            @Value("${jwt.cookie-secure}") boolean secure,
            @Value("${jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.secure = secure;
        this.maxAge = Duration.ofSeconds(refreshTtlSeconds);
    }

    public void set(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(token, maxAge).toString());
    }

    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString());
    }

    private ResponseCookie cookie(String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .path(PATH)
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }
}

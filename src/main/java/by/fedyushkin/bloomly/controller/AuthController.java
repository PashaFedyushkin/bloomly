package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.*;
import by.fedyushkin.bloomly.security.RefreshCookie;
import by.fedyushkin.bloomly.service.AuthService;
import by.fedyushkin.bloomly.service.IssuedSession;
import by.fedyushkin.bloomly.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshCookie refreshCookie;

    @PostMapping("/code")
    public SendCodeResponse sendCode(@RequestBody SendCodeRequest request) {
        return authService.sendCode(request.getPhone());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody VerifyCodeRequest request, HttpServletResponse response) {
        IssuedSession session = authService.login(request.getPhone(), request.getCode());
        refreshCookie.set(response, session.refreshToken());
        return session.response();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        IssuedSession session = authService.refresh(refreshToken);
        refreshCookie.set(response, session.refreshToken());
        return session.response();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = RefreshCookie.NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        refreshCookie.clear(response);
    }
}

package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.AuthResponse;
import by.fedyushkin.bloomly.dto.SendCodeRequest;
import by.fedyushkin.bloomly.dto.SendCodeResponse;
import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.dto.VerifyCodeRequest;
import by.fedyushkin.bloomly.security.UserPrincipal;
import by.fedyushkin.bloomly.service.AuthService;
import by.fedyushkin.bloomly.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/code")
    public SendCodeResponse sendCode(@RequestBody SendCodeRequest request) {
        return authService.sendCode(request.getPhone());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody VerifyCodeRequest request) {
        return authService.login(request.getPhone(), request.getCode());
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userService.getById(principal.id());
    }
}

package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.RegistrationDto;
import by.fedyushkin.bloomly.dto.SendCodeRequest;
import by.fedyushkin.bloomly.dto.SendCodeResponse;
import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.dto.VerifyCodeRequest;
import by.fedyushkin.bloomly.service.AuthService;
import by.fedyushkin.bloomly.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final AuthService authService;

    @PostMapping("/user")
    public UserDto registrationUser(@RequestBody RegistrationDto request) {
        return registrationService.registerUser(request);
    }

    @PostMapping("/code")
    public SendCodeResponse sendCode(@RequestBody SendCodeRequest request) {
        return registrationService.sendCode(request.getPhone());
    }

    @PostMapping("/check/phone")
    @ResponseStatus(HttpStatus.OK)
    public void checkPhone(@RequestBody VerifyCodeRequest request) {
        registrationService.checkPhone(request.getPhone(), request.getCode());
    }
}

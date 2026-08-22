package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.AuthResponse;
import by.fedyushkin.bloomly.dto.SendCodeResponse;

public interface AuthService {

    SendCodeResponse sendCode(String phone);

    AuthResponse login(String phone, String code);
}

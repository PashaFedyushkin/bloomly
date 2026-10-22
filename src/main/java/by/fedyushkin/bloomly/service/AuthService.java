package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.SendCodeResponse;

public interface AuthService {

    SendCodeResponse sendCode(String phone);

    IssuedSession login(String phone, String code);

    IssuedSession refresh(String refreshToken);

    void logout(String refreshToken);
}

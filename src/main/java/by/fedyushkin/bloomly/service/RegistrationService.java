package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.RegistrationDto;
import by.fedyushkin.bloomly.dto.SendCodeResponse;
import by.fedyushkin.bloomly.dto.UserDto;

public interface RegistrationService {

    UserDto registerMaster(UserDto dto);

    UserDto registerUser(RegistrationDto dto);

    SendCodeResponse sendCode(String phone);

    void checkPhone(String phone, String code);
}

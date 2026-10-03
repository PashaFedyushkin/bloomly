package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.RegistrationDto;
import by.fedyushkin.bloomly.dto.SendCodeResponse;
import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.notification.TelegramNotificationBot;
import by.fedyushkin.bloomly.util.PhoneNumbers;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static by.fedyushkin.bloomly.notification.TelegramNotificationBot.BOT_HINT;

@Service
@AllArgsConstructor
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;
    private final OtpService otpService;
    private final TelegramNotificationBot telegramNotificationBot;

    @Override
    public UserDto registerMaster(UserDto dto) {
        return userService.createMaster(dto);
    }

    @Override
    public UserDto registerUser(RegistrationDto dto) {
        return userService.create(updateUserDto(dto));
    }

    @Override
    public SendCodeResponse sendCode(String phone) {
        String normalizedPhone = requirePhone(phone);
        checkExists(normalizedPhone);
        String code = otpService.generateAndStore(normalizedPhone);
        String text = "Код для подтверждения телефона в Bloomly: " + code
                + "\n\nКод действителен " + (otpService.getTtlSeconds() / 60)
                + " мин. Никому его не сообщайте.";

        boolean sent = telegramNotificationBot.sendMessageByPhone(normalizedPhone, text);
        if (!sent) {
            otpService.invalidate(normalizedPhone);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BOT_HINT);
        }

        return new SendCodeResponse("Код отправлен в Telegram", otpService.getTtlSeconds());
    }

    private void checkExists(String phone) {
        String normalized = requirePhone(phone);
        if (userService.isPhoneExists(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с таким номером уже существует");
        }
    }

    @Override
    public void checkPhone(String phone, String code) {
        String normalizedPhone = requirePhone(phone);
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите код из Telegram");
        }

        otpService.verify(normalizedPhone, code.trim());
    }

    private UserDto updateUserDto(RegistrationDto dto) {
        UserDto userDto = createUserDto(dto);
        userDto.setRoles(Arrays.asList("CLIENT_ROLE"));
        return userDto;
    }

    private UserDto createUserDto(RegistrationDto dto) {
        UserDto userDto = new UserDto();
        userDto.setName(dto.getName());
        userDto.setLastName(dto.getLastName());
        userDto.setPhone(PhoneNumbers.normalize(dto.getPhone()));
        return userDto;
    }

    private String requirePhone(String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите номер телефона");
        }
        return normalized;
    }
}

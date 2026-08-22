package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.AuthResponse;
import by.fedyushkin.bloomly.dto.SendCodeResponse;
import by.fedyushkin.bloomly.dto.UserDto;
import by.fedyushkin.bloomly.entity.Role;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.notification.TelegramNotificationBot;
import by.fedyushkin.bloomly.repository.RoleRepository;
import by.fedyushkin.bloomly.repository.UserRepository;
import by.fedyushkin.bloomly.security.JwtService;
import by.fedyushkin.bloomly.util.PhoneNumbers;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String CLIENT_ROLE = "CLIENT_ROLE";
    private static final String BOT_HINT =
            "Не удалось отправить код. Откройте Telegram-бота Bloomly, нажмите /start и поделитесь номером телефона.";

    private final OtpService otpService;
    private final TelegramNotificationBot telegramNotificationBot;
    private final TelegramChatBindingService telegramChatBindingService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public SendCodeResponse sendCode(String phone) {
        String normalizedPhone = requirePhone(phone);
        String code = otpService.generateAndStore(normalizedPhone);
        String text = "Код для входа в Bloomly: " + code
                + "\n\nКод действителен " + (otpService.getTtlSeconds() / 60)
                + " мин. Никому его не сообщайте.";

        boolean sent = telegramNotificationBot.sendMessageByPhone(normalizedPhone, text);
        if (!sent) {
            otpService.invalidate(normalizedPhone);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BOT_HINT);
        }

        return new SendCodeResponse("Код отправлен в Telegram", otpService.getTtlSeconds());
    }

    @Override
    public AuthResponse login(String phone, String code) {
        String normalizedPhone = requirePhone(phone);
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите код из Telegram");
        }

        otpService.verify(normalizedPhone, code.trim());
        User user = userRepository.findByPhone(normalizedPhone)
                .orElseGet(() -> registerClient(normalizedPhone));

        String token = jwtService.generateToken(user);
        UserDto userDto = userService.getById(user.getId());
        return new AuthResponse(token, "Bearer", userDto);
    }

    private User registerClient(String phone) {
        Role clientRole = roleRepository.findByName(CLIENT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Роль " + CLIENT_ROLE + " не найдена"));

        User user = new User();
        user.setPhone(phone);
        user.setName(telegramChatBindingService.findDisplayName(phone).orElse("User"));
        user.setCreationDate(LocalDateTime.now());
        user.setTelegramChatId(telegramChatBindingService.findChatId(phone).orElse(null));
        user.setRoles(new ArrayList<>(List.of(clientRole)));
        return userRepository.save(user);
    }

    private String requirePhone(String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите номер телефона");
        }
        return normalized;
    }
}

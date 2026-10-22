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
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import by.fedyushkin.bloomly.util.PhoneNumbers;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static by.fedyushkin.bloomly.notification.TelegramNotificationBot.BOT_HINT;

@Service
@AllArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String CLIENT_ROLE = "CLIENT_ROLE";

    private final OtpService otpService;
    private final TelegramNotificationBot telegramNotificationBot;
    private final TelegramChatBindingService telegramChatBindingService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenStoreService refreshTokenStore;
    private final UserService userService;

    @Override
    public SendCodeResponse sendCode(String phone) {
        String normalizedPhone = requirePhone(phone);
        if (!userService.isPhoneExists(normalizedPhone)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователя с таким номером не существует");
        }
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
    public IssuedSession login(String phone, String code) {
        String normalizedPhone = requirePhone(phone);
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите код из Telegram");
        }

        otpService.verify(normalizedPhone, code.trim());
        User user = userRepository.findByPhone(normalizedPhone)
                .orElseGet(() -> registerClient(normalizedPhone));

        return issue(user);
    }

    @Override
    public IssuedSession refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is missing");
        }

        Claims claims;
        try {
            claims = jwtService.parseToken(refreshToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }
        if (!jwtService.isRefreshToken(claims) || claims.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        String storedUserId = refreshTokenStore.consume(claims.getId());
        if (storedUserId == null || !storedUserId.equals(claims.getSubject())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired");
        }

        User user = userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid or expired"));
        return issue(user);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        try {
            Claims claims = jwtService.parseToken(refreshToken);
            if (jwtService.isRefreshToken(claims) && claims.getId() != null) {
                refreshTokenStore.delete(claims.getId());
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // The cookie is cleared even when the token is already invalid.
        }
    }

    private IssuedSession issue(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        JwtService.RefreshToken refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenStore.save(refreshToken.jti(), user.getId());
        UserDto userDto = userService.getById(user.getId());
        AuthResponse response = new AuthResponse(accessToken, "Bearer", jwtService.getAccessTtlSeconds(), userDto);
        return new IssuedSession(response, refreshToken.value());
    }

    private User registerClient(String phone) {
        Role clientRole = roleRepository.findByName(CLIENT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Роль " + CLIENT_ROLE + " не найдена"));

        User user = new User();
        user.setPhone(phone);
        user.setName(telegramChatBindingService.findDisplayName(phone).orElse("User"));
        user.setCreationDate(LocalDateTime.now());
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

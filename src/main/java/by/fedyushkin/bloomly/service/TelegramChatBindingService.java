package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.repository.UserRepository;
import by.fedyushkin.bloomly.util.PhoneNumbers;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TelegramChatBindingService {

    private static final String CHAT_KEY_PREFIX = "telegram:chat:";
    private static final String NAME_KEY_PREFIX = "telegram:name:";

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;

    @Transactional
    public void bind(String phone, Long chatId, String displayName) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null || chatId == null) {
            return;
        }

        redis.opsForValue().set(CHAT_KEY_PREFIX + normalized, String.valueOf(chatId));
        if (displayName != null && !displayName.isBlank()) {
            redis.opsForValue().set(NAME_KEY_PREFIX + normalized, displayName.trim());
        }

        userRepository.findByPhone(normalized).ifPresentOrElse(
                user -> {
                    user.setTelegramChatId(chatId);
                    userRepository.save(user);
                },
                () -> {
                    User user = new User();
                    user.setPhone(normalized);
                    user.setTelegramChatId(chatId);
                    userRepository.save(user);
                });
    }

    public Optional<Long> findChatId(String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            return Optional.empty();
        }

        String cached = redis.opsForValue().get(CHAT_KEY_PREFIX + normalized);
        if (cached != null) {
            return Optional.of(Long.parseLong(cached));
        }

        return userRepository.findByPhone(normalized)
                .map(User::getTelegramChatId)
                .filter(Objects::nonNull)
                .map(chatId -> {
                    redis.opsForValue().set(CHAT_KEY_PREFIX + normalized, String.valueOf(chatId));
                    return chatId;
                });
    }

    public Optional<String> findDisplayName(String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(redis.opsForValue().get(NAME_KEY_PREFIX + normalized))
                .filter(name -> !name.isBlank());
    }
}

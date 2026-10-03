package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.entity.Telegram;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.repository.TelegramRepository;
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

    private final TelegramRepository telegramRepository;

    @Transactional
    public void bind(String phone, Long chatId, String displayName) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null || chatId == null) {
            return;
        }

        telegramRepository.findByPhone(normalized).ifPresentOrElse(
                telegram -> {
                    telegram.setChatId(chatId);
                    telegram.setDisplayName(displayName);
                    telegramRepository.save(telegram);
                },
                () -> {
                    Telegram telegram = new Telegram();
                    telegram.setPhone(normalized);
                    telegram.setChatId(chatId);
                    telegramRepository.save(telegram);
                });


    }

    public Optional<Long> findChatId(String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            return Optional.empty();
        }

        return telegramRepository.findByPhone(normalized)
                .map(Telegram::getChatId);
    }

    public Optional<String> findDisplayName(String phone) {
        String normalized = PhoneNumbers.normalize(phone);
        if (normalized == null) {
            return Optional.empty();
        }

        return telegramRepository.findByPhone(normalized)
                .map(Telegram::getDisplayName);
    }
}

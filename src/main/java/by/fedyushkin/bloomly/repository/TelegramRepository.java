package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Telegram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramRepository extends JpaRepository<Telegram, Long> {

    Optional<Telegram> findByPhone(String phone);
}

package by.fedyushkin.bloomly.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "telegram")
public class Telegram {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "telegram_id_generator")
    @SequenceGenerator(name = "telegram_id_generator", sequenceName = "telegram_id_seq", allocationSize = 1)
    private Long id;
    private String phone;
    private Long chatId;
    private String displayName;
}

package by.fedyushkin.bloomly.dto;

import by.fedyushkin.bloomly.enums.CSessionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionDto {

    private Long id;
    private LocalDateTime date;
    private Float finalPrice;
    private CSessionStatus status;
    private Long masterId;
    private Long serviceId;
    private Long userId;
}

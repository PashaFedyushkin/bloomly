package by.fedyushkin.bloomly.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionDto {

    private Long id;
    private LocalDateTime date;
    private Float finalPrice;
    private Long masterId;
    private Long serviceId;
}

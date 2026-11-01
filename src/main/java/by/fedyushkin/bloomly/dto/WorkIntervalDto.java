package by.fedyushkin.bloomly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
public class WorkIntervalDto {

    private Long id;
    private Long workDayId;

    @Schema(description = "Время начала", example = "08:00:00", type = "string", format = "time")
    private LocalTime start;

    @Schema(description = "Время конца", example = "19:00:00", type = "string", format = "time")
    private LocalTime end;
}

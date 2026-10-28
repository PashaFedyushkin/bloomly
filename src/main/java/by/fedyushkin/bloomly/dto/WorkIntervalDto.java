package by.fedyushkin.bloomly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WorkIntervalDto {

    @Schema(description = "Дата", example = "2026-10-28", type = "string", format = "date")
    private LocalDate date;

    @Schema(description = "Время начала", example = "08:00:00", type = "string", format = "time")
    private LocalTime start;

    @Schema(description = "Время конца", example = "19:00:00", type = "string", format = "time")
    private LocalTime end;
}

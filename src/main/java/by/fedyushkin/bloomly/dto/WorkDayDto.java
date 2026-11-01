package by.fedyushkin.bloomly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WorkDayDto {

    private Long id;
    private Long masterId;

    @Schema(description = "Дата", example = "2026-10-28", type = "string", format = "date")
    private LocalDate date;

    private List<WorkIntervalDto> intervals;
}

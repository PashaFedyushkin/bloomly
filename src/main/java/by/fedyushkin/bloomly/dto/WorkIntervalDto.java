package by.fedyushkin.bloomly.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class WorkIntervalDto {

    private LocalTime start;
    private LocalTime end;
}

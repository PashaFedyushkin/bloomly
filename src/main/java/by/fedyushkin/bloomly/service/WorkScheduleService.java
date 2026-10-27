package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.WorkIntervalDto;

import java.util.List;

public interface WorkScheduleService {

    List<WorkIntervalDto> get(Long masterId);

    List<WorkIntervalDto> replace(Long masterId, List<WorkIntervalDto> intervals);
}

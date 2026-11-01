package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.WorkDayDto;

import java.util.List;

public interface WorkDayService {

    List<WorkDayDto> getAll();

    WorkDayDto getById(Long workDayId);

    List<WorkDayDto> getByMasterId(Long masterId);

    WorkDayDto create(Long masterId, WorkDayDto workDayDto);

    WorkDayDto update(Long masterId, Long workDayId, WorkDayDto workDayDto);

    void delete(Long masterId, Long workDayId);
}

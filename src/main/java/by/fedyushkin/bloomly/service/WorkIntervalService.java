package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.WorkIntervalDto;

import java.util.List;

public interface WorkIntervalService {

    List<WorkIntervalDto> getAll();

    WorkIntervalDto getById(Long workIntervalId);

    List<WorkIntervalDto> getByWorkDayId(Long workDayId);

    WorkIntervalDto create(Long masterId, WorkIntervalDto workIntervalDto);

    WorkIntervalDto update(Long masterId, WorkIntervalDto workIntervalDto);

    void delete(Long masterId, Long workIntervalId, Long workDayId);
}

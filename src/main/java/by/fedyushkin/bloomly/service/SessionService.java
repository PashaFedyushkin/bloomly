package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.SessionDto;

import java.util.List;

public interface SessionService {

    List<SessionDto> getAll();

    List<SessionDto> getByMasterId(Long masterId);

    List<SessionDto> getByServiceId(Long serviceId);

    List<SessionDto> getByMasterIdAndServiceId(Long masterId, Long serviceId);

    SessionDto getById(Long sessionId);

    SessionDto create(SessionDto sessionDto);

    SessionDto update(Long sessionId, SessionDto sessionDto);

    void delete(Long sessionId);
}

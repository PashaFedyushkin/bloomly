package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.SessionDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionService {

    List<SessionDto> getAll();

    List<SessionDto> getByMasterId(Long masterId);

    List<SessionDto> getByServiceId(Long serviceId);

    List<SessionDto> getByMasterIdAndServiceId(Long masterId, Long serviceId);

    List<SessionDto> getByUserId(Long userId);

    List<SessionDto> getByMasterIdAndDateBetween(Long masterId, LocalDateTime dateFrom, LocalDateTime dateTo);

    List<SessionDto> getMyNearestSessions(Long userId);

    SessionDto getById(Long sessionId);

    SessionDto create(SessionDto sessionDto);

    SessionDto update(Long sessionId, SessionDto sessionDto);

    SessionDto addUser(Long sessionId, Long userId);

    List<LocalDateTime> availableStarts(Long masterId, Long serviceId, LocalDate date);

    SessionDto book(Long userId, Long masterId, Long serviceId, LocalDateTime start);

    void delete(Long sessionId);
}

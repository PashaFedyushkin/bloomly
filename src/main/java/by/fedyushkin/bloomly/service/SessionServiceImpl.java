package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.SessionDto;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.Service;
import by.fedyushkin.bloomly.entity.Session;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.enums.CSessionStatus;
import by.fedyushkin.bloomly.entity.WorkInterval;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.ServiceRepository;
import by.fedyushkin.bloomly.repository.SessionRepository;
import by.fedyushkin.bloomly.repository.UserRepository;
import by.fedyushkin.bloomly.repository.WorkIntervalRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@org.springframework.stereotype.Service
@AllArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private static final int SLOT_STEP_MINUTES = 10;

    private final SessionRepository repository;
    private final MasterRepository masterRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final WorkIntervalRepository workIntervalRepository;
    private final ReviewService reviewService;

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getByMasterId(Long masterId) {
        return repository.findByMasterId(masterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getByServiceId(Long serviceId) {
        return repository.findByServiceId(serviceId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getByMasterIdAndServiceId(Long masterId, Long serviceId) {
        return repository.findByMasterIdAndServiceId(masterId, serviceId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getByMasterIdAndDateBetween(Long masterId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateFrom and dateTo are required");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dateFrom must be before or equal to dateTo");
        }
        return repository.findByMasterIdAndDateBetween(masterId, dateFrom, dateTo).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<SessionDto> getMyNearestSessions(Long userId) {
        return repository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Session::getDate))
                .limit(3)
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionDto getById(Long sessionId) {
        return toDto(getSessionOrThrow(sessionId));
    }

    @Override
    public SessionDto create(SessionDto sessionDto) {
        Session session = new Session();
        apply(session, sessionDto);
        return toDto(repository.save(session));
    }

    @Override
    public SessionDto update(Long sessionId, SessionDto sessionDto) {
        Session session = getSessionOrThrow(sessionId);
        apply(session, sessionDto);
        return toDto(repository.save(session));
    }

    @Override
    public SessionDto addUser(Long sessionId, Long userId) {
        Session session = getSessionOrThrow(sessionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        session.setUser(user);
        if (session.getStatus() == null || session.getStatus() == CSessionStatus.CREATED) {
            session.setStatus(CSessionStatus.BOOKED);
        }
        return toDto(repository.save(session));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> availableStarts(Long masterId, Long serviceId, LocalDate date) {
        if (masterId == null || serviceId == null || date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите мастера, процедуру и день");
        }
        Service service = resolveService(serviceId);
        if (service.getMaster() == null || !masterId.equals(service.getMaster().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Процедура не принадлежит этому мастеру");
        }
        int duration = requireDuration(service);
        List<WorkInterval> intervals = workIntervalRepository.findByMasterIdAndWorkDateOrderByStartTimeAsc(masterId, date);
        List<Session> busy = repository.findOccupying(
                masterId,
                date.atStartOfDay().minusDays(1),
                date.plusDays(1).atStartOfDay(),
                CSessionStatus.CANCELLED
        );

        LocalDateTime now = LocalDateTime.now();
        List<LocalDateTime> starts = new ArrayList<>();
        for (WorkInterval interval : intervals) {
            if (interval.getWorkDate() == null || interval.getStartTime() == null || interval.getEndTime() == null) {
                continue;
            }
            LocalDateTime windowStart = interval.getWorkDate().atTime(interval.getStartTime());
            LocalDateTime windowEnd = interval.getWorkDate().atTime(interval.getEndTime());
            LocalDateTime cursor = alignUp(windowStart);
            while (!cursor.plusMinutes(duration).isAfter(windowEnd)) {
                if (!cursor.isBefore(now) && !overlaps(cursor, duration, busy)) {
                    starts.add(cursor);
                }
                cursor = cursor.plusMinutes(SLOT_STEP_MINUTES);
            }
        }
        return starts;
    }

    @Override
    public SessionDto book(Long userId, Long masterId, Long serviceId, LocalDateTime start) {
        if (masterId == null || serviceId == null || start == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите мастера, процедуру и время");
        }
        masterRepository.findByIdForUpdate(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
        LocalDateTime slot = start.withSecond(0).withNano(0);
        if (!availableStarts(masterId, serviceId, slot.toLocalDate()).contains(slot)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Это время недоступно");
        }
        Service service = resolveService(serviceId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
        Session session = new Session();
        session.setDate(slot);
        session.setFinalPrice(service.getPrice());
        session.setStatus(CSessionStatus.BOOKED);
        session.setMaster(service.getMaster());
        session.setService(service);
        session.setUser(user);
        return toDto(repository.save(session));
    }

    @Override
    public void delete(Long sessionId) {
        if (!repository.existsById(sessionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + sessionId);
        }
        reviewService.deleteBySessionId(sessionId);
        repository.deleteById(sessionId);
    }

    private void apply(Session session, SessionDto sessionDto) {
        session.setDate(sessionDto.getDate());
        session.setFinalPrice(sessionDto.getFinalPrice());
        if (sessionDto.getStatus() != null) {
            session.setStatus(sessionDto.getStatus());
        } else if (session.getStatus() == null) {
            session.setStatus(CSessionStatus.CREATED);
        }
        session.setMaster(resolveMaster(sessionDto.getMasterId()));
        session.setService(resolveService(sessionDto.getServiceId()));
    }

    private Master resolveMaster(Long masterId) {
        if (masterId == null) {
            return null;
        }
        return masterRepository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
    }

    private Service resolveService(Long serviceId) {
        if (serviceId == null) {
            return null;
        }
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found: " + serviceId));
    }

    private int requireDuration(Service service) {
        Integer duration = service.getDurationMinutes();
        if (duration == null || duration <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "У процедуры не указана длительность");
        }
        return duration;
    }

    private LocalDateTime alignUp(LocalDateTime time) {
        LocalDateTime floor = time.withSecond(0).withNano(0);
        int remainder = floor.getMinute() % SLOT_STEP_MINUTES;
        if (remainder == 0 && !floor.isBefore(time)) {
            return floor;
        }
        if (remainder == 0) {
            return floor.plusMinutes(SLOT_STEP_MINUTES);
        }
        return floor.plusMinutes(SLOT_STEP_MINUTES - remainder);
    }

    private boolean overlaps(LocalDateTime start, int durationMinutes, List<Session> busy) {
        LocalDateTime end = start.plusMinutes(durationMinutes);
        for (Session session : busy) {
            if (session.getDate() == null) {
                continue;
            }
            int busyDuration = 0;
            if (session.getService() != null && session.getService().getDurationMinutes() != null) {
                busyDuration = Math.max(session.getService().getDurationMinutes(), 0);
            }
            LocalDateTime busyEnd = session.getDate().plusMinutes(busyDuration);
            if (start.isBefore(busyEnd) && end.isAfter(session.getDate())) {
                return true;
            }
        }
        return false;
    }

    private Session getSessionOrThrow(Long sessionId) {
        return repository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found: " + sessionId));
    }

    private SessionDto toDto(Session session) {
        SessionDto dto = new SessionDto();
        dto.setId(session.getId());
        dto.setDate(session.getDate());
        dto.setFinalPrice(session.getFinalPrice());
        dto.setStatus(session.getStatus());
        if (session.getMaster() != null) {
            dto.setMasterId(session.getMaster().getId());
        }
        if (session.getService() != null) {
            dto.setServiceId(session.getService().getId());
        }
        if (session.getUser() != null) {
            dto.setUserId(session.getUser().getId());
        }
        return dto;
    }
}

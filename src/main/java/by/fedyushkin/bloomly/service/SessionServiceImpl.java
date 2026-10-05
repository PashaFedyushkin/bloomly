package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.SessionDto;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.Service;
import by.fedyushkin.bloomly.entity.Session;
import by.fedyushkin.bloomly.entity.User;
import by.fedyushkin.bloomly.enums.CSessionStatus;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.ServiceRepository;
import by.fedyushkin.bloomly.repository.SessionRepository;
import by.fedyushkin.bloomly.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@org.springframework.stereotype.Service
@AllArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {

    private final SessionRepository repository;
    private final MasterRepository masterRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
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

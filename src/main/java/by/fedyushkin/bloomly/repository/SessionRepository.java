package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByMasterId(Long masterId);

    List<Session> findByServiceId(Long serviceId);

    List<Session> findByMasterIdAndServiceId(Long masterId, Long serviceId);

    List<Session> findByUserId(Long userId);

    List<Session> findByMasterIdAndDateBetween(Long masterId, LocalDateTime dateFrom, LocalDateTime dateTo);
}

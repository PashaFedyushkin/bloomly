package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Session;
import by.fedyushkin.bloomly.enums.CSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByMasterId(Long masterId);

    List<Session> findByServiceId(Long serviceId);

    List<Session> findByMasterIdAndServiceId(Long masterId, Long serviceId);

    List<Session> findByUserId(Long userId);

    List<Session> findByMasterIdAndDateBetween(Long masterId, LocalDateTime dateFrom, LocalDateTime dateTo);

    @Query("""
            select s from Session s
            left join fetch s.service
            where s.master.id = :masterId
              and s.date >= :from
              and s.date < :to
              and (s.status is null or s.status <> :cancelled)
            """)
    List<Session> findOccupying(
            @Param("masterId") Long masterId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("cancelled") CSessionStatus cancelled
    );
}

package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.WorkInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface WorkIntervalRepository extends JpaRepository<WorkInterval, Long> {

    Optional<WorkInterval> findByStartTimeAndEndTime(LocalTime startTime, LocalTime endTime);

    @Query("select count(d) from WorkInterval i join i.workDays d where i.id = :id")
    long countWorkDays(@Param("id") Long id);

    List<WorkInterval> findByWorkDaysIdOrderByStartTimeAsc(Long workDayId);

    @Query("""
            select i from WorkInterval i
            join i.workDays d
            where d.master.id = :masterId
              and d.workDate = :workDate
            order by i.startTime asc
            """)
    List<WorkInterval> findByMasterIdAndWorkDate(@Param("masterId") Long masterId, @Param("workDate") LocalDate workDate);
}

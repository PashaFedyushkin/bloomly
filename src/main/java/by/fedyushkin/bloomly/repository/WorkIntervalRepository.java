package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.WorkInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkIntervalRepository extends JpaRepository<WorkInterval, Long> {

    List<WorkInterval> findByWorkDayIdOrderByStartTimeAsc(Long workDayId);

    @Query("""
            select i from WorkInterval i
            where i.workDay.master.id = :masterId
              and i.workDay.workDate = :workDate
            order by i.startTime asc
            """)
    List<WorkInterval> findByMasterIdAndWorkDate(@Param("masterId") Long masterId, @Param("workDate") LocalDate workDate);
}

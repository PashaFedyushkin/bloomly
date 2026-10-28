package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.WorkInterval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkIntervalRepository extends JpaRepository<WorkInterval, Long> {

    List<WorkInterval> findByMasterIdOrderByWorkDateAscStartTimeAsc(Long masterId);

    List<WorkInterval> findByMasterIdAndWorkDateOrderByStartTimeAsc(Long masterId, LocalDate workDate);

    void deleteByMasterId(Long masterId);
}

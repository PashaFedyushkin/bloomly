package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.WorkInterval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkIntervalRepository extends JpaRepository<WorkInterval, Long> {

    List<WorkInterval> findByMasterIdOrderByStartTimeAsc(Long masterId);

    void deleteByMasterId(Long masterId);
}

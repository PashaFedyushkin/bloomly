package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {

    List<WorkDay> findByMasterIdOrderByWorkDateAsc(Long masterId);

    boolean existsByMasterIdAndWorkDate(Long masterId, LocalDate workDate);

    boolean existsByMasterIdAndWorkDateAndIdNot(Long masterId, LocalDate workDate, Long id);
}

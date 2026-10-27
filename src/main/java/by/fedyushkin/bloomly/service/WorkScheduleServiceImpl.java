package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.WorkIntervalDto;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.WorkInterval;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.WorkIntervalRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private final WorkIntervalRepository repository;
    private final MasterRepository masterRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkIntervalDto> get(Long masterId) {
        getMasterOrThrow(masterId);
        return repository.findByMasterIdOrderByStartTimeAsc(masterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<WorkIntervalDto> replace(Long masterId, List<WorkIntervalDto> intervals) {
        Master master = getMasterOrThrow(masterId);
        List<WorkIntervalDto> normalized = normalize(intervals);
        repository.deleteByMasterId(masterId);
        for (WorkIntervalDto dto : normalized) {
            WorkInterval interval = new WorkInterval();
            interval.setMaster(master);
            interval.setStartTime(dto.getStart());
            interval.setEndTime(dto.getEnd());
            repository.save(interval);
        }
        return normalized;
    }

    private List<WorkIntervalDto> normalize(List<WorkIntervalDto> intervals) {
        if (intervals == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите интервалы работы");
        }
        List<WorkIntervalDto> sorted = new ArrayList<>();
        for (WorkIntervalDto interval : intervals) {
            if (interval == null || interval.getStart() == null || interval.getEnd() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите время начала и конца");
            }
            WorkIntervalDto copy = new WorkIntervalDto();
            copy.setStart(interval.getStart().withSecond(0).withNano(0));
            copy.setEnd(interval.getEnd().withSecond(0).withNano(0));
            if (!copy.getStart().isBefore(copy.getEnd())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Время начала должно быть раньше времени конца");
            }
            sorted.add(copy);
        }
        sorted.sort(Comparator.comparing(WorkIntervalDto::getStart));
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).getStart().isBefore(sorted.get(i - 1).getEnd())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Интервалы работы пересекаются");
            }
        }
        return sorted;
    }

    private WorkIntervalDto toDto(WorkInterval interval) {
        WorkIntervalDto dto = new WorkIntervalDto();
        dto.setStart(interval.getStartTime());
        dto.setEnd(interval.getEndTime());
        return dto;
    }

    private Master getMasterOrThrow(Long masterId) {
        return masterRepository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
    }
}

package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.WorkDayDto;
import by.fedyushkin.bloomly.dto.WorkIntervalDto;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.WorkDay;
import by.fedyushkin.bloomly.entity.WorkInterval;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.WorkDayRepository;
import by.fedyushkin.bloomly.repository.WorkIntervalRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class WorkDayServiceImpl implements WorkDayService {

    private final WorkDayRepository repository;
    private final WorkIntervalRepository workIntervalRepository;
    private final MasterRepository masterRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkDayDto> getAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(WorkDay::getWorkDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkDayDto getById(Long workDayId) {
        return toDto(getWorkDayOrThrow(workDayId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkDayDto> getByMasterId(Long masterId) {
        return repository.findByMasterIdOrderByWorkDateAsc(masterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public WorkDayDto create(Long masterId, WorkDayDto workDayDto) {
        Master master = getMasterOrThrow(masterId);
        if (workDayDto == null || workDayDto.getDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите дату");
        }
        if (repository.existsByMasterIdAndWorkDate(masterId, workDayDto.getDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Рабочий день на эту дату уже есть");
        }
        WorkDay workDay = new WorkDay();
        workDay.setMaster(master);
        workDay.setWorkDate(workDayDto.getDate());
        replaceIntervals(workDay, workDayDto.getIntervals());
        return toDto(repository.save(workDay));
    }

    @Override
    public WorkDayDto update(Long masterId, Long workDayId, WorkDayDto workDayDto) {
        WorkDay workDay = getOwnedWorkDay(masterId, workDayId);
        if (workDayDto == null || workDayDto.getDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите дату");
        }
        if (repository.existsByMasterIdAndWorkDateAndIdNot(masterId, workDayDto.getDate(), workDayId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Рабочий день на эту дату уже есть");
        }
        workDay.setWorkDate(workDayDto.getDate());
        if (workDayDto.getIntervals() != null) {
            replaceIntervals(workDay, workDayDto.getIntervals());
        }
        return toDto(repository.save(workDay));
    }

    @Override
    public void delete(Long masterId, Long workDayId) {
        repository.delete(getOwnedWorkDay(masterId, workDayId));
    }

    private void replaceIntervals(WorkDay workDay, List<WorkIntervalDto> intervals) {
        List<WorkIntervalDto> normalized = normalize(intervals);
        if (workDay.getWorkIntervals() == null) {
            workDay.setWorkIntervals(new ArrayList<>());
        }
        workDay.getWorkIntervals().clear();
        for (WorkIntervalDto dto : normalized) {
            WorkInterval interval = findOrCreate(dto.getStart(), dto.getEnd());
            boolean alreadyLinked = workDay.getWorkIntervals().stream()
                    .anyMatch(item -> interval.getId().equals(item.getId()));
            if (!alreadyLinked) {
                workDay.getWorkIntervals().add(interval);
            }
        }
    }

    private WorkInterval findOrCreate(LocalTime start, LocalTime end) {
        return workIntervalRepository.findByStartTimeAndEndTime(start, end).orElseGet(() -> {
            WorkInterval interval = new WorkInterval();
            interval.setStartTime(start);
            interval.setEndTime(end);
            return workIntervalRepository.save(interval);
        });
    }

    private List<WorkIntervalDto> normalize(List<WorkIntervalDto> intervals) {
        if (intervals == null) {
            return List.of();
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

    private WorkDay getWorkDayOrThrow(Long workDayId) {
        return repository.findById(workDayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work day not found: " + workDayId));
    }

    private WorkDay getOwnedWorkDay(Long masterId, Long workDayId) {
        WorkDay workDay = getWorkDayOrThrow(workDayId);
        if (workDay.getMaster() == null || !masterId.equals(workDay.getMaster().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work day not found: " + workDayId);
        }
        return workDay;
    }

    private Master getMasterOrThrow(Long masterId) {
        return masterRepository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
    }

    private WorkDayDto toDto(WorkDay workDay) {
        WorkDayDto dto = new WorkDayDto();
        dto.setId(workDay.getId());
        dto.setDate(workDay.getWorkDate());
        if (workDay.getMaster() != null) {
            dto.setMasterId(workDay.getMaster().getId());
        }
        List<WorkInterval> intervals = workDay.getWorkIntervals() == null ? List.of() : workDay.getWorkIntervals();
        dto.setIntervals(intervals.stream().map(interval -> toIntervalDto(interval, workDay.getId())).toList());
        return dto;
    }

    private WorkIntervalDto toIntervalDto(WorkInterval interval, Long workDayId) {
        WorkIntervalDto dto = new WorkIntervalDto();
        dto.setId(interval.getId());
        dto.setWorkDayId(workDayId);
        dto.setStart(interval.getStartTime());
        dto.setEnd(interval.getEndTime());
        return dto;
    }
}

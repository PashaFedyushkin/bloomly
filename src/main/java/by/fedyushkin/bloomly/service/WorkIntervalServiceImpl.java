package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.WorkIntervalDto;
import by.fedyushkin.bloomly.entity.WorkDay;
import by.fedyushkin.bloomly.entity.WorkInterval;
import by.fedyushkin.bloomly.repository.WorkDayRepository;
import by.fedyushkin.bloomly.repository.WorkIntervalRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class WorkIntervalServiceImpl implements WorkIntervalService {

    private final WorkIntervalRepository repository;
    private final WorkDayRepository workDayRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkIntervalDto> getAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(WorkInterval::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkIntervalDto getById(Long workIntervalId) {
        return toDto(getIntervalOrThrow(workIntervalId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkIntervalDto> getByWorkDayId(Long workDayId) {
        getWorkDayOrThrow(workDayId);
        return repository.findByWorkDayIdOrderByStartTimeAsc(workDayId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public WorkIntervalDto create(Long masterId, WorkIntervalDto workIntervalDto) {
        if (workIntervalDto == null || workIntervalDto.getWorkDayId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите рабочий день");
        }
        WorkDay workDay = getOwnedWorkDay(masterId, workIntervalDto.getWorkDayId());
        LocalTime start = normalizeTime(workIntervalDto.getStart());
        LocalTime end = normalizeTime(workIntervalDto.getEnd());
        assertRange(start, end);
        assertNoOverlap(workDay.getId(), null, start, end);
        WorkInterval interval = new WorkInterval();
        interval.setWorkDay(workDay);
        interval.setStartTime(start);
        interval.setEndTime(end);
        return toDto(repository.save(interval));
    }

    @Override
    public WorkIntervalDto update(Long masterId, Long workIntervalId, WorkIntervalDto workIntervalDto) {
        WorkInterval interval = getOwnedInterval(masterId, workIntervalId);
        if (workIntervalDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите время начала и конца");
        }
        WorkDay workDay = interval.getWorkDay();
        if (workIntervalDto.getWorkDayId() != null && !workIntervalDto.getWorkDayId().equals(workDay.getId())) {
            workDay = getOwnedWorkDay(masterId, workIntervalDto.getWorkDayId());
        }
        LocalTime start = normalizeTime(workIntervalDto.getStart());
        LocalTime end = normalizeTime(workIntervalDto.getEnd());
        assertRange(start, end);
        assertNoOverlap(workDay.getId(), interval.getId(), start, end);
        interval.setWorkDay(workDay);
        interval.setStartTime(start);
        interval.setEndTime(end);
        return toDto(repository.save(interval));
    }

    @Override
    public void delete(Long masterId, Long workIntervalId) {
        repository.delete(getOwnedInterval(masterId, workIntervalId));
    }

    private void assertNoOverlap(Long workDayId, Long excludeIntervalId, LocalTime start, LocalTime end) {
        for (WorkInterval interval : repository.findByWorkDayIdOrderByStartTimeAsc(workDayId)) {
            if (excludeIntervalId != null && excludeIntervalId.equals(interval.getId())) {
                continue;
            }
            if (interval.getStartTime() == null || interval.getEndTime() == null) {
                continue;
            }
            if (start.isBefore(interval.getEndTime()) && interval.getStartTime().isBefore(end)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Интервалы работы пересекаются");
            }
        }
    }

    private LocalTime normalizeTime(LocalTime time) {
        if (time == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите время начала и конца");
        }
        return time.withSecond(0).withNano(0);
    }

    private void assertRange(LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Время начала должно быть раньше времени конца");
        }
    }

    private WorkInterval getIntervalOrThrow(Long workIntervalId) {
        return repository.findById(workIntervalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work interval not found: " + workIntervalId));
    }

    private WorkDay getWorkDayOrThrow(Long workDayId) {
        return workDayRepository.findById(workDayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work day not found: " + workDayId));
    }

    private WorkDay getOwnedWorkDay(Long masterId, Long workDayId) {
        WorkDay workDay = getWorkDayOrThrow(workDayId);
        if (workDay.getMaster() == null || !masterId.equals(workDay.getMaster().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work day not found: " + workDayId);
        }
        return workDay;
    }

    private WorkInterval getOwnedInterval(Long masterId, Long workIntervalId) {
        WorkInterval interval = getIntervalOrThrow(workIntervalId);
        WorkDay workDay = interval.getWorkDay();
        if (workDay == null || workDay.getMaster() == null || !masterId.equals(workDay.getMaster().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work interval not found: " + workIntervalId);
        }
        return interval;
    }

    private WorkIntervalDto toDto(WorkInterval interval) {
        WorkIntervalDto dto = new WorkIntervalDto();
        dto.setId(interval.getId());
        dto.setStart(interval.getStartTime());
        dto.setEnd(interval.getEndTime());
        if (interval.getWorkDay() != null) {
            dto.setWorkDayId(interval.getWorkDay().getId());
        }
        return dto;
    }
}

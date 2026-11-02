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
import java.util.ArrayList;
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
                .map(interval -> toDto(interval, singleWorkDayId(interval)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkIntervalDto getById(Long workIntervalId) {
        WorkInterval interval = getIntervalOrThrow(workIntervalId);
        return toDto(interval, singleWorkDayId(interval));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkIntervalDto> getByWorkDayId(Long workDayId) {
        getWorkDayOrThrow(workDayId);
        return repository.findByWorkDaysIdOrderByStartTimeAsc(workDayId).stream()
                .map(interval -> toDto(interval, workDayId))
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
        WorkInterval interval = findOrCreate(start, end);
        if (!isLinked(workDay, interval)) {
            assertNoOverlap(workDay.getId(), null, start, end);
            link(workDay, interval);
        }
        return toDto(interval, workDay.getId());
    }

    @Override
    public WorkIntervalDto update(Long masterId, WorkIntervalDto workIntervalDto) {
        WorkInterval current = getIntervalOrThrow(workIntervalDto.getId());
        List<WorkDay> owned = daysOfMaster(current, masterId);
        if (owned.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work interval not found: " + workIntervalDto.getId());
        }

        WorkDay source = resolveSourceDay(owned, workIntervalDto.getWorkDayId(), masterId);
        WorkDay targetDay = source;
        if (workIntervalDto.getWorkDayId() != null && !workIntervalDto.getWorkDayId().equals(source.getId())) {
            targetDay = getOwnedWorkDay(masterId, workIntervalDto.getWorkDayId());
        }

        LocalTime start = normalizeTime(workIntervalDto.getStart());
        LocalTime end = normalizeTime(workIntervalDto.getEnd());
        assertRange(start, end);
        assertNoOverlap(targetDay.getId(), current.getId(), start, end);

        WorkInterval interval = findOrCreate(start, end);
        if (!current.getId().equals(interval.getId()) || !source.getId().equals(targetDay.getId())) {
            unlink(source, current);
        }
        if (!isLinked(targetDay, interval)) {
            link(targetDay, interval);
        }
        deleteIfUnused(current);
        return toDto(interval, targetDay.getId());
    }

    @Override
    public void delete(Long masterId, Long workIntervalId, Long workDayId) {
        WorkInterval interval = getIntervalOrThrow(workIntervalId);
        List<WorkDay> owned = daysOfMaster(interval, masterId);
        if (workDayId != null) {
            WorkDay workDay = getOwnedWorkDay(masterId, workDayId);
            if (!isLinked(workDay, interval)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work interval not found: " + workIntervalId);
            }
            unlink(workDay, interval);
        } else if (owned.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work interval not found: " + workIntervalId);
        } else {
            owned.forEach(day -> unlink(day, interval));
        }
        deleteIfUnused(interval);
    }

    private WorkDay resolveSourceDay(List<WorkDay> owned, Long requestedWorkDayId, Long masterId) {
        if (requestedWorkDayId == null) {
            if (owned.size() == 1) {
                return owned.get(0);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите рабочий день");
        }
        for (WorkDay day : owned) {
            if (requestedWorkDayId.equals(day.getId())) {
                return day;
            }
        }
        if (owned.size() == 1) {
            return owned.get(0);
        }
        getOwnedWorkDay(masterId, requestedWorkDayId);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Work interval not found: " + requestedWorkDayId);
    }

    private WorkInterval findOrCreate(LocalTime start, LocalTime end) {
        return repository.findByStartTimeAndEndTime(start, end).orElseGet(() -> {
            WorkInterval interval = new WorkInterval();
            interval.setStartTime(start);
            interval.setEndTime(end);
            return repository.save(interval);
        });
    }

    private void link(WorkDay workDay, WorkInterval interval) {
        if (workDay.getWorkIntervals() == null) {
            workDay.setWorkIntervals(new ArrayList<>());
        }
        workDay.getWorkIntervals().add(interval);
        if (interval.getWorkDays() == null) {
            interval.setWorkDays(new ArrayList<>());
        }
        if (interval.getWorkDays().stream().noneMatch(day -> workDay.getId().equals(day.getId()))) {
            interval.getWorkDays().add(workDay);
        }
    }

    private void unlink(WorkDay workDay, WorkInterval interval) {
        if (workDay.getWorkIntervals() != null) {
            workDay.getWorkIntervals().removeIf(item -> interval.getId().equals(item.getId()));
        }
        if (interval.getWorkDays() != null) {
            interval.getWorkDays().removeIf(day -> workDay.getId().equals(day.getId()));
        }
    }

    private boolean isLinked(WorkDay workDay, WorkInterval interval) {
        return workDay.getWorkIntervals() != null && workDay.getWorkIntervals().stream()
                .anyMatch(item -> interval.getId().equals(item.getId()));
    }

    private void deleteIfUnused(WorkInterval interval) {
        if (interval.getId() == null) {
            return;
        }
        repository.flush();
        if (repository.countWorkDays(interval.getId()) == 0) {
            repository.delete(interval);
        }
    }

    private void assertNoOverlap(Long workDayId, Long excludeIntervalId, LocalTime start, LocalTime end) {
        for (WorkInterval interval : repository.findByWorkDaysIdOrderByStartTimeAsc(workDayId)) {
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

    private List<WorkDay> daysOfMaster(WorkInterval interval, Long masterId) {
        if (interval.getWorkDays() == null) {
            return List.of();
        }
        return interval.getWorkDays().stream()
                .filter(day -> day.getMaster() != null && masterId.equals(day.getMaster().getId()))
                .toList();
    }

    private Long singleWorkDayId(WorkInterval interval) {
        if (interval.getWorkDays() != null && interval.getWorkDays().size() == 1) {
            return interval.getWorkDays().get(0).getId();
        }
        return null;
    }

    private WorkIntervalDto toDto(WorkInterval interval, Long workDayId) {
        WorkIntervalDto dto = new WorkIntervalDto();
        dto.setId(interval.getId());
        dto.setWorkDayId(workDayId);
        dto.setStart(interval.getStartTime());
        dto.setEnd(interval.getEndTime());
        return dto;
    }
}

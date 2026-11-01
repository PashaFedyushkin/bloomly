package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.WorkIntervalDto;
import by.fedyushkin.bloomly.service.WorkIntervalService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/work-intervals")
@AllArgsConstructor
public class WorkIntervalController extends BaseController {

    private final WorkIntervalService workIntervalService;

    @GetMapping
    public List<WorkIntervalDto> getAll(@RequestParam(required = false) Long workDayId) {
        if (workDayId != null) {
            return workIntervalService.getByWorkDayId(workDayId);
        }
        return workIntervalService.getAll();
    }

    @GetMapping("/{id}")
    public WorkIntervalDto getById(@PathVariable Long id) {
        return workIntervalService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkIntervalDto create(@RequestBody WorkIntervalDto workIntervalDto) {
        return workIntervalService.create(getCurrentUserId(), workIntervalDto);
    }

    @PutMapping("/{id}")
    public WorkIntervalDto update(@PathVariable Long id, @RequestBody WorkIntervalDto workIntervalDto) {
        return workIntervalService.update(getCurrentUserId(), id, workIntervalDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workIntervalService.delete(getCurrentUserId(), id);
    }
}

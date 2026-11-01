package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.WorkDayDto;
import by.fedyushkin.bloomly.service.WorkDayService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/work-days")
@AllArgsConstructor
public class WorkDayController extends BaseController {

    private final WorkDayService workDayService;

    @GetMapping
    public List<WorkDayDto> getAll(@RequestParam(required = false) Long masterId) {
        if (masterId != null) {
            return workDayService.getByMasterId(masterId);
        }
        return workDayService.getAll();
    }

    @GetMapping("/my")
    public List<WorkDayDto> getCurrent() {
        return workDayService.getByMasterId(getCurrentUserId());
    }

    @GetMapping("/{id}")
    public WorkDayDto getById(@PathVariable Long id) {
        return workDayService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkDayDto create(@RequestBody WorkDayDto workDayDto) {
        return workDayService.create(getCurrentUserId(), workDayDto);
    }

    @PutMapping("/{id}")
    public WorkDayDto update(@PathVariable Long id, @RequestBody WorkDayDto workDayDto) {
        return workDayService.update(getCurrentUserId(), id, workDayDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workDayService.delete(getCurrentUserId(), id);
    }
}

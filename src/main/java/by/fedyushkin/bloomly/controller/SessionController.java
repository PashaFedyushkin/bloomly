package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.SessionDto;
import by.fedyushkin.bloomly.service.SessionService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/sessions")
@AllArgsConstructor
public class SessionController extends BaseController{

    private final SessionService sessionService;

    @GetMapping
    public List<SessionDto> getAll(
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo
    ) {
        if (masterId != null && dateFrom != null && dateTo != null) {
            return sessionService.getByMasterIdAndDateBetween(masterId, dateFrom, dateTo);
        }
        if (masterId != null && serviceId != null) {
            return sessionService.getByMasterIdAndServiceId(masterId, serviceId);
        }
        if (masterId != null) {
            return sessionService.getByMasterId(masterId);
        }
        if (serviceId != null) {
            return sessionService.getByServiceId(serviceId);
        }
        if (userId != null) {
            return sessionService.getByUserId(userId);
        }
        return sessionService.getAll();
    }

    @GetMapping("/my")
    public List<SessionDto> getAll(
            @Parameter(description = "Дата с", example = "2025-01-01T00:00:00", schema = @Schema(type = "string", format = "date-time"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @Parameter(description = "Дата по", example = "2025-01-01T23:00:00", schema = @Schema(type = "string", format = "date-time"))
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo
    ) {
        if (dateFrom != null && dateTo != null) {
            return sessionService.getByMasterIdAndDateBetween(getCurrentUserId(), dateFrom, dateTo);
        }
        return Collections.emptyList();
    }

    @GetMapping("/{id}")
    public SessionDto getById(@PathVariable Long id) {
        return sessionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto create(@RequestBody SessionDto sessionDto) {
        sessionDto.setMasterId(getCurrentUserId());
        return sessionService.create(sessionDto);
    }

    @PutMapping("/{id}")
    public SessionDto update(@PathVariable Long id, @RequestBody SessionDto sessionDto) {
        sessionDto.setMasterId(getCurrentUserId());
        return sessionService.update(id, sessionDto);
    }

    @PutMapping("/user/{id}")
    public SessionDto addUser(@PathVariable Long id) {
        return sessionService.addUser(id, getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sessionService.delete(id);
    }

    @GetMapping("/my/nearest")
    public List<SessionDto> getById() {
        return sessionService.getMyNearestSessions(getCurrentUserId());
    }
}

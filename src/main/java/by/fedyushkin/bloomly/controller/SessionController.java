package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.SessionDto;
import by.fedyushkin.bloomly.service.SessionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@AllArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public List<SessionDto> getAll(
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) Long serviceId
    ) {
        if (masterId != null && serviceId != null) {
            return sessionService.getByMasterIdAndServiceId(masterId, serviceId);
        }
        if (masterId != null) {
            return sessionService.getByMasterId(masterId);
        }
        if (serviceId != null) {
            return sessionService.getByServiceId(serviceId);
        }
        return sessionService.getAll();
    }

    @GetMapping("/{id}")
    public SessionDto getById(@PathVariable Long id) {
        return sessionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDto create(@RequestBody SessionDto sessionDto) {
        return sessionService.create(sessionDto);
    }

    @PutMapping("/{id}")
    public SessionDto update(@PathVariable Long id, @RequestBody SessionDto sessionDto) {
        return sessionService.update(id, sessionDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sessionService.delete(id);
    }
}

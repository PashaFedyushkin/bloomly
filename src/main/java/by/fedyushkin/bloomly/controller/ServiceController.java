package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.ServiceDto;
import by.fedyushkin.bloomly.service.ServiceService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
@AllArgsConstructor
public class ServiceController extends BaseController{

    private final ServiceService serviceService;

    @GetMapping
    public List<ServiceDto> getAll(@RequestParam(required = false) Long masterId) {
        if (masterId != null) {
            return serviceService.getByMasterId(masterId);
        }
        return serviceService.getAll();
    }

    @GetMapping("/my")
    public List<ServiceDto> getMyServices() {
        return serviceService.getByMasterId(getCurrentUserId());
    }

    @GetMapping("/{id}")
    public ServiceDto getById(@PathVariable Long id) {
        return serviceService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceDto create(@RequestBody ServiceDto serviceDto) {
        serviceDto.setMasterId(getCurrentUserId());
        return serviceService.create(serviceDto);
    }

    @PutMapping("/{id}")
    public ServiceDto update(@PathVariable Long id, @RequestBody ServiceDto serviceDto) {
        return serviceService.update(id, serviceDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        serviceService.delete(id);
    }
}

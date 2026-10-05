package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.ServiceDto;
import by.fedyushkin.bloomly.service.ServiceService;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/services")
@AllArgsConstructor
public class ServiceController extends BaseController{

    private final ServiceService serviceService;

    @GetMapping
    public Page<ServiceDto> getAll(
            @RequestParam(required = false) Long masterId,
            @RequestParam(required = false) String categoryName,
            @ParameterObject Pageable pageable
    ) {
        return serviceService.getAll(masterId, categoryName, pageable);
    }

    @GetMapping("/my")
    public Page<ServiceDto> getMyServices(
            @RequestParam(required = false) String categoryName,
            @ParameterObject Pageable pageable
    ) {
        return serviceService.getAll(getCurrentUserId(), categoryName, pageable);
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

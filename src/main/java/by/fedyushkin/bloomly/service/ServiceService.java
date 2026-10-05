package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.ServiceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceService {

    Page<ServiceDto> getAll(Long masterId, String categoryName, Pageable pageable);

    ServiceDto getById(Long serviceId);

    ServiceDto create(ServiceDto serviceDto);

    ServiceDto update(Long serviceId, ServiceDto serviceDto);

    void delete(Long serviceId);
}

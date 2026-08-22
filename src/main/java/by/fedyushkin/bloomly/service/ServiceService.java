package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.ServiceDto;

import java.util.List;

public interface ServiceService {

    List<ServiceDto> getAll();

    List<ServiceDto> getByMasterId(Long masterId);

    ServiceDto getById(Long serviceId);

    ServiceDto create(ServiceDto serviceDto);

    ServiceDto update(Long serviceId, ServiceDto serviceDto);

    void delete(Long serviceId);
}

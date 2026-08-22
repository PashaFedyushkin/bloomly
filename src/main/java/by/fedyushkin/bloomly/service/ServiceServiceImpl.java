package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.ServiceDto;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.Service;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.ServiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@org.springframework.stereotype.Service
@AllArgsConstructor
@Transactional
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository repository;
    private final MasterRepository masterRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDto> getByMasterId(Long masterId) {
        return repository.findByMasterId(masterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDto getById(Long serviceId) {
        return toDto(getServiceOrThrow(serviceId));
    }

    @Override
    public ServiceDto create(ServiceDto serviceDto) {
        Service service = new Service();
        apply(service, serviceDto);
        return toDto(repository.save(service));
    }

    @Override
    public ServiceDto update(Long serviceId, ServiceDto serviceDto) {
        Service service = getServiceOrThrow(serviceId);
        apply(service, serviceDto);
        return toDto(repository.save(service));
    }

    @Override
    public void delete(Long serviceId) {
        if (!repository.existsById(serviceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found: " + serviceId);
        }
        repository.deleteById(serviceId);
    }

    private void apply(Service service, ServiceDto serviceDto) {
        service.setName(serviceDto.getName());
        service.setDescription(serviceDto.getDescription());
        service.setPrice(serviceDto.getPrice());
        service.setMaster(resolveMaster(serviceDto.getMasterId()));
    }

    private Master resolveMaster(Long masterId) {
        if (masterId == null) {
            return null;
        }
        return masterRepository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
    }

    private Service getServiceOrThrow(Long serviceId) {
        return repository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found: " + serviceId));
    }

    private ServiceDto toDto(Service service) {
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        if (service.getMaster() != null) {
            dto.setMasterId(service.getMaster().getId());
        }
        return dto;
    }
}

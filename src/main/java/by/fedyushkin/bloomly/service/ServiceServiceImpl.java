package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.CategoryDto;
import by.fedyushkin.bloomly.dto.ServiceDto;
import by.fedyushkin.bloomly.entity.Category;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.Service;
import by.fedyushkin.bloomly.repository.CategoryRepository;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.ServiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service
@AllArgsConstructor
@Transactional
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository repository;
    private final MasterRepository masterRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceDto> getAll(Long masterId, String categoryName, Pageable pageable) {
        return repository.findFiltered(masterId, normalize(categoryName), stablePage(pageable))
                .map(this::toDto);
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

    private Pageable stablePage(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id"));
    }

    private String normalize(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        return categoryName.trim();
    }

    private void apply(Service service, ServiceDto serviceDto) {
        service.setName(serviceDto.getName());
        service.setDescription(serviceDto.getDescription());
        service.setPrice(serviceDto.getPrice());
        service.setDurationMinutes(serviceDto.getDurationMinutes());
        service.setMaster(resolveMaster(serviceDto.getMasterId()));
        service.setCategory(resolveCategory(serviceDto.getCategoryId()));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + categoryId));
    }

    private Master resolveMaster(Long masterId) {
        if (masterId == null) {
            return null;
        }
        return masterRepository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master not found: " + masterId));
    }

    private CategoryDto toCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
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
        dto.setDurationMinutes(service.getDurationMinutes());
        if (service.getMaster() != null) {
            dto.setMasterId(service.getMaster().getId());
        }
        if (service.getCategory() != null) {
            dto.setCategoryId(service.getCategory().getId());
            dto.setCategory(toCategoryDto(service.getCategory()));
        }
        return dto;
    }
}

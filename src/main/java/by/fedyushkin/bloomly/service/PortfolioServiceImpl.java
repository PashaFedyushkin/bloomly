package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.PortfolioDto;
import by.fedyushkin.bloomly.entity.Master;
import by.fedyushkin.bloomly.entity.Portfolio;
import by.fedyushkin.bloomly.repository.MasterRepository;
import by.fedyushkin.bloomly.repository.PortfolioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private static final int MAX_PHOTOS = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private final PortfolioRepository repository;
    private final MasterRepository masterRepository;
    private final MinioStorageService minioStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioDto getById(Long portfolioId) {
        return toDto(getPortfolioOrThrow(portfolioId));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioDto getByMasterId(Long masterId) {
        return repository.findByMasterId(masterId)
                .map(this::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Портфолио не найдено для мастера: " + masterId));
    }

    @Override
    public PortfolioDto create(Long masterId, String description, List<MultipartFile> photos) {
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мастер не найден: " + masterId));
        if (repository.existsByMasterId(masterId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Для данного мастера портфолио уже существует: " + masterId);
        }

        List<MultipartFile> imageFiles = requirePhotos(photos);
        List<String> uploadedKeys = uploadPhotos(masterId, imageFiles);
        try {
            Portfolio portfolio = new Portfolio();
            portfolio.setMaster(master);
            portfolio.setDescription(normalizeDescription(description));
            portfolio.setPhotoKeys(new ArrayList<>(uploadedKeys));
            return toDto(repository.save(portfolio));
        } catch (RuntimeException e) {
            uploadedKeys.forEach(minioStorageService::delete);
            throw e;
        }
    }

    @Override
    public PortfolioDto update(Long portfolioId, String description, List<MultipartFile> photos) {
        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        if (description != null) {
            portfolio.setDescription(normalizeDescription(description));
        }

        List<MultipartFile> imageFiles = requirePhotos(photos);
        if (imageFiles.isEmpty()) {
            return toDto(repository.save(portfolio));
        }

        List<String> oldKeys = new ArrayList<>(portfolio.getPhotoKeys());
        List<String> uploadedKeys = uploadPhotos(portfolio.getMaster().getId(), imageFiles);
        try {
            portfolio.setPhotoKeys(new ArrayList<>(uploadedKeys));
            PortfolioDto dto = toDto(repository.save(portfolio));
            oldKeys.forEach(minioStorageService::delete);
            return dto;
        } catch (RuntimeException e) {
            uploadedKeys.forEach(minioStorageService::delete);
            throw e;
        }
    }

    @Override
    public void delete(Long portfolioId) {
        deletePortfolio(getPortfolioOrThrow(portfolioId));
    }

    @Override
    public void deleteByMasterId(Long masterId) {
        repository.findByMasterId(masterId).ifPresent(this::deletePortfolio);
    }

    private void deletePortfolio(Portfolio portfolio) {
        List<String> keys = new ArrayList<>(portfolio.getPhotoKeys());
        repository.delete(portfolio);
        keys.forEach(minioStorageService::delete);
    }

    private List<String> uploadPhotos(Long masterId, List<MultipartFile> photos) {
        List<String> keys = new ArrayList<>();
        try {
            for (MultipartFile photo : photos) {
                keys.add(minioStorageService.uploadPortfolioPhoto(masterId, photo));
            }
            return keys;
        } catch (RuntimeException e) {
            keys.forEach(minioStorageService::delete);
            throw e;
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be at most 2000 characters");
        }
        return trimmed;
    }

    private List<MultipartFile> requirePhotos(List<MultipartFile> photos) {
        List<MultipartFile> files = photos == null
                ? List.of()
                : photos.stream().filter(file -> file != null && !file.isEmpty()).toList();
        if (files.size() > MAX_PHOTOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At most 10 photos are allowed");
        }
        return files;
    }

    private Portfolio getPortfolioOrThrow(Long portfolioId) {
        return repository.findById(portfolioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found: " + portfolioId));
    }

    private PortfolioDto toDto(Portfolio portfolio) {
        PortfolioDto dto = new PortfolioDto();
        dto.setId(portfolio.getId());
        dto.setDescription(portfolio.getDescription());
        if (portfolio.getMaster() != null) {
            dto.setMasterId(portfolio.getMaster().getId());
        }
        dto.setPhotoUrls(portfolio.getPhotoKeys().stream()
                .map(minioStorageService::presignedUrl)
                .toList());
        return dto;
    }
}

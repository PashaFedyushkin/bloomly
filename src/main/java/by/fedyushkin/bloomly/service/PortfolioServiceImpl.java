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

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

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
    public List<PortfolioDto> getByMasterId(Long masterId) {
        return repository.findByMasterIdOrderByIdAsc(masterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PortfolioDto create(Long masterId, String description, MultipartFile photo) {
        Master master = masterRepository.findById(masterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Мастер не найден: " + masterId));
        if (photo == null || photo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo is required");
        }

        String uploadedKey = minioStorageService.uploadPortfolioPhoto(masterId, photo);
        try {
            Portfolio portfolio = new Portfolio();
            portfolio.setMaster(master);
            portfolio.setDescription(normalizeDescription(description));
            portfolio.setPhotoKey(uploadedKey);
            return toDto(repository.save(portfolio));
        } catch (RuntimeException e) {
            minioStorageService.delete(uploadedKey);
            throw e;
        }
    }

    @Override
    public PortfolioDto update(Long masterId, Long portfolioId, String description, MultipartFile photo) {
        Portfolio portfolio = getOwnedPortfolio(masterId, portfolioId);
        if (description != null) {
            portfolio.setDescription(normalizeDescription(description));
        }
        String oldKey = null;
        String uploadedKey = null;
        if (photo != null && !photo.isEmpty()) {
            uploadedKey = minioStorageService.uploadPortfolioPhoto(masterId, photo);
            oldKey = portfolio.getPhotoKey();
            portfolio.setPhotoKey(uploadedKey);
        }
        try {
            PortfolioDto dto = toDto(repository.save(portfolio));
            if (oldKey != null) {
                minioStorageService.delete(oldKey);
            }
            return dto;
        } catch (RuntimeException e) {
            if (uploadedKey != null) {
                minioStorageService.delete(uploadedKey);
            }
            throw e;
        }
    }

    @Override
    public void delete(Long masterId, Long portfolioId) {
        deletePortfolio(getOwnedPortfolio(masterId, portfolioId));
    }

    @Override
    public void deleteByMasterId(Long masterId) {
        repository.findByMasterIdOrderByIdAsc(masterId).forEach(this::deletePortfolio);
    }

    private void deletePortfolio(Portfolio portfolio) {
        String photoKey = portfolio.getPhotoKey();
        repository.delete(portfolio);
        if (photoKey != null) {
            minioStorageService.delete(photoKey);
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

    private Portfolio getPortfolioOrThrow(Long portfolioId) {
        return repository.findById(portfolioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found: " + portfolioId));
    }

    private Portfolio getOwnedPortfolio(Long masterId, Long portfolioId) {
        Portfolio portfolio = getPortfolioOrThrow(portfolioId);
        if (portfolio.getMaster() == null || !masterId.equals(portfolio.getMaster().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Portfolio not found: " + portfolioId);
        }
        return portfolio;
    }

    private PortfolioDto toDto(Portfolio portfolio) {
        PortfolioDto dto = new PortfolioDto();
        dto.setId(portfolio.getId());
        dto.setDescription(portfolio.getDescription());
        if (portfolio.getMaster() != null) {
            dto.setMasterId(portfolio.getMaster().getId());
        }
        if (portfolio.getPhotoKey() != null) {
            dto.setPhotoUrl(minioStorageService.presignedUrl(portfolio.getPhotoKey()));
        }
        return dto;
    }
}

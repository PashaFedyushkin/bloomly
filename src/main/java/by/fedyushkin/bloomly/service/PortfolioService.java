package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.PortfolioDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PortfolioService {

    List<PortfolioDto> getAll();

    PortfolioDto getById(Long portfolioId);

    PortfolioDto getByMasterId(Long masterId);

    PortfolioDto create(Long masterId, String description, List<MultipartFile> photos);

    PortfolioDto update(Long portfolioId, String description, List<MultipartFile> photos);

    void delete(Long portfolioId);

    void deleteByMasterId(Long masterId);
}

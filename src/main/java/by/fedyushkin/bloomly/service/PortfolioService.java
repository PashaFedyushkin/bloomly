package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.PortfolioDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PortfolioService {

    List<PortfolioDto> getAll();

    PortfolioDto getById(Long portfolioId);

    List<PortfolioDto> getByMasterId(Long masterId);

    PortfolioDto create(Long masterId, String description, MultipartFile photo);

    PortfolioDto update(Long masterId, Long portfolioId, String description, MultipartFile photo);

    void delete(Long masterId, Long portfolioId);

    void deleteByMasterId(Long masterId);
}

package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.PortfolioDto;
import by.fedyushkin.bloomly.service.PortfolioService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/portfolios")
@AllArgsConstructor
public class PortfolioController extends BaseController{

    private final PortfolioService portfolioService;

    @GetMapping
    public List<PortfolioDto> getAll(@RequestParam(required = false) Long masterId) {
        if (masterId != null) {
            return List.of(portfolioService.getByMasterId(masterId));
        }
        return portfolioService.getAll();
    }

    @GetMapping("/{id}")
    public PortfolioDto getById(@PathVariable Long id) {
        return portfolioService.getById(id);
    }

    @GetMapping("/my")
    public PortfolioDto getCurrent() {
        return portfolioService.getByMasterId(getCurrentUserId());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioDto create(
            @RequestParam(required = false) String description,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos
    ) {
        return portfolioService.create(getCurrentUserId(), description, photos);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PortfolioDto update(
            @RequestParam(required = false) String description,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos
    ) {
        return portfolioService.update(getCurrentUserId(), description, photos);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        portfolioService.delete(getCurrentUserId());
    }
}

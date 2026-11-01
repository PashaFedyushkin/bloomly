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
            return portfolioService.getByMasterId(masterId);
        }
        return portfolioService.getAll();
    }

    @GetMapping("/my")
    public List<PortfolioDto> getCurrent() {
        return portfolioService.getByMasterId(getCurrentUserId());
    }

    @GetMapping("/favorites/{masterId}")
    public List<PortfolioDto> getFavorites(@PathVariable Long masterId) {
        return portfolioService.getFavoritesByMasterId(masterId);
    }

    @GetMapping("/{id}")
    public PortfolioDto getById(@PathVariable Long id) {
        return portfolioService.getById(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioDto create(
            @RequestParam(required = false) String description,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(required = false, defaultValue = "false") boolean favorite
    ) {
        return portfolioService.create(getCurrentUserId(), description, photo, favorite);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PortfolioDto update(
            @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(required = false) Boolean favorite
    ) {
        return portfolioService.update(getCurrentUserId(), id, description, photo, favorite);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        portfolioService.delete(getCurrentUserId(), id);
    }
}

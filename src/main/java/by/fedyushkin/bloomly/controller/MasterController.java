package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.MasterDto;
import by.fedyushkin.bloomly.dto.MasterFullDto;
import by.fedyushkin.bloomly.service.MasterService;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/masters")
@AllArgsConstructor
public class MasterController extends BaseController {

    private final MasterService masterService;

    @GetMapping
    public Page<MasterDto> getAll(@ParameterObject Pageable pageable) {
        return masterService.getAll(pageable);
    }

    @GetMapping("/all")
    public Page<MasterFullDto> getMasters(@ParameterObject Pageable pageable) {
        return masterService.getMasters(pageable);
    }

    @GetMapping("/{id}")
    public MasterFullDto getById(@PathVariable Long id) {
        return masterService.getById(id);
    }

    @GetMapping("/current")
    public MasterFullDto getById() {
        return masterService.getById(getCurrentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MasterDto create(@RequestBody MasterDto masterDto) {
        masterDto.setUserId(getCurrentUserId());
        return masterService.create(masterDto);
    }

    @PutMapping
    public MasterDto update(@RequestBody MasterDto masterDto) {
        masterDto.setUserId(getCurrentUserId());
        return masterService.update(getCurrentUserId(), masterDto);
    }

    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MasterDto updatePhoto(@RequestParam("photo") MultipartFile photo) {
        return masterService.updatePhoto(getCurrentUserId(), photo);
    }

    @DeleteMapping("/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePhoto() {
        masterService.deletePhoto(getCurrentUserId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        masterService.delete(getCurrentUserId());
    }
}

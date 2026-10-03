package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.MasterDto;
import by.fedyushkin.bloomly.dto.MasterFullDto;
import by.fedyushkin.bloomly.service.MasterService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masters")
@AllArgsConstructor
public class MasterController extends BaseController {

    private final MasterService masterService;

    @GetMapping
    public List<MasterDto> getAll() {
        return masterService.getAll();
    }

    @GetMapping("/all")
    public List<MasterFullDto> getMasters() {
        return masterService.getMasters();
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

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() {
        masterService.delete(getCurrentUserId());
    }
}

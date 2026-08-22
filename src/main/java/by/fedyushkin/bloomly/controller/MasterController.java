package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.MasterDto;
import by.fedyushkin.bloomly.service.MasterService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/masters")
@AllArgsConstructor
public class MasterController {

    private final MasterService masterService;

    @GetMapping
    public List<MasterDto> getAll() {
        return masterService.getAll();
    }

    @GetMapping("/{id}")
    public MasterDto getById(@PathVariable Long id) {
        return masterService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MasterDto create(@RequestBody MasterDto masterDto) {
        return masterService.create(masterDto);
    }

    @PutMapping("/{id}")
    public MasterDto update(@PathVariable Long id, @RequestBody MasterDto masterDto) {
        return masterService.update(id, masterDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        masterService.delete(id);
    }
}

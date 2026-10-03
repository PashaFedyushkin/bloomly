package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.AddressDto;
import by.fedyushkin.bloomly.service.AddressService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@AllArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public List<AddressDto> getAll() {
        return addressService.getAll();
    }

    @GetMapping("/{id}")
    public AddressDto getById(@PathVariable Long id) {
        return addressService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressDto create(@RequestBody AddressDto addressDto) {
        return addressService.create(addressDto);
    }

    @PutMapping("/{id}")
    public AddressDto update(@PathVariable Long id, @RequestBody AddressDto addressDto) {
        return addressService.update(id, addressDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        addressService.delete(id);
    }
}

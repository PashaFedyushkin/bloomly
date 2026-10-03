package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.AddressDto;
import by.fedyushkin.bloomly.entity.Address;
import by.fedyushkin.bloomly.repository.AddressRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getById(Long addressId) {
        return toDto(getAddressOrThrow(addressId));
    }

    @Override
    public AddressDto create(AddressDto addressDto) {
        Address address = new Address();
        apply(address, addressDto);
        return toDto(repository.save(address));
    }

    @Override
    public AddressDto update(Long addressId, AddressDto addressDto) {
        Address address = getAddressOrThrow(addressId);
        apply(address, addressDto);
        return toDto(repository.save(address));
    }

    @Override
    public void delete(Long addressId) {
        if (!repository.existsById(addressId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found: " + addressId);
        }
        repository.deleteById(addressId);
    }

    private void apply(Address address, AddressDto addressDto) {
        address.setCountry(addressDto.getCountry());
        address.setRegion(addressDto.getRegion());
        address.setLocality(addressDto.getLocality());
        address.setDistrict(addressDto.getDistrict());
        address.setPlace(addressDto.getPlace());
        address.setStreet(addressDto.getStreet());
        address.setHouse(addressDto.getHouse());
        address.setApartment(addressDto.getApartment());
    }

    private Address getAddressOrThrow(Long addressId) {
        return repository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found: " + addressId));
    }

    private AddressDto toDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setCountry(address.getCountry());
        dto.setRegion(address.getRegion());
        dto.setLocality(address.getLocality());
        dto.setDistrict(address.getDistrict());
        dto.setPlace(address.getPlace());
        dto.setStreet(address.getStreet());
        dto.setHouse(address.getHouse());
        dto.setApartment(address.getApartment());
        return dto;
    }
}

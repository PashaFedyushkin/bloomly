package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.AddressDto;

import java.util.List;

public interface AddressService {

    List<AddressDto> getAll();

    AddressDto getById(Long addressId);

    AddressDto create(AddressDto addressDto);

    AddressDto update(Long addressId, AddressDto addressDto);

    void delete(Long addressId);
}

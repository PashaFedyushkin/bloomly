package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.MasterDto;

import java.util.List;

public interface MasterService {

    List<MasterDto> getAll();

    MasterDto getById(Long masterId);

    MasterDto create(MasterDto masterDto);

    MasterDto update(Long masterId, MasterDto masterDto);

    void delete(Long masterId);
}

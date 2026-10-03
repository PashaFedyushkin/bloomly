package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.MasterDto;
import by.fedyushkin.bloomly.dto.MasterFullDto;

import java.util.List;

public interface MasterService {

    List<MasterDto> getAll();

    List<MasterFullDto> getMasters();

    MasterFullDto getById(Long masterId);

    MasterDto create(MasterDto masterDto);

    MasterDto update(Long masterId, MasterDto masterDto);

    void delete(Long masterId);
}

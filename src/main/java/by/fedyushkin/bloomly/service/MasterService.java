package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.MasterDto;
import by.fedyushkin.bloomly.dto.MasterFullDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MasterService {

    Page<MasterDto> getAll(Pageable pageable);

    Page<MasterFullDto> getMasters(Pageable pageable);

    MasterFullDto getById(Long masterId);

    MasterDto create(MasterDto masterDto);

    MasterDto update(Long masterId, MasterDto masterDto);

    void delete(Long masterId);
}

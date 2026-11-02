package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    Page<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long categoryId);

    List<CategoryDto> getByMasterId(Long masterId);

    List<String> getListByMasterId(Long masterId);
}

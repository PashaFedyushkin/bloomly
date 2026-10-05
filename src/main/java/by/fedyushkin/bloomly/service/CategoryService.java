package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.CategoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryDto> getAll(Pageable pageable);

    CategoryDto getById(Long categoryId);
}

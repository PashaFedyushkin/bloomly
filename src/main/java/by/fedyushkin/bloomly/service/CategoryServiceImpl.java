package by.fedyushkin.bloomly.service;

import by.fedyushkin.bloomly.dto.CategoryDto;
import by.fedyushkin.bloomly.entity.Category;
import by.fedyushkin.bloomly.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getAll(Pageable pageable) {
        return repository.findAll(stablePage(pageable)).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long categoryId) {
        return toDto(getCategoryOrThrow(categoryId));
    }

    private Pageable stablePage(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id"));
    }

    private Category getCategoryOrThrow(Long categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + categoryId));
    }

    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        return dto;
    }
}

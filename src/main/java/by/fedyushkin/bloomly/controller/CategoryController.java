package by.fedyushkin.bloomly.controller;

import by.fedyushkin.bloomly.dto.CategoryDto;
import by.fedyushkin.bloomly.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Page<CategoryDto> getAll(@ParameterObject Pageable pageable) {
        return categoryService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public CategoryDto getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }
}

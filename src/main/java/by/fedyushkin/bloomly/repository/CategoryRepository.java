package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

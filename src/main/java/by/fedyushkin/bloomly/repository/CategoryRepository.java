package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("""
            select c from Category c
            where c.id in (
                select s.category.id from Service s
                where s.master.id = :masterId
                  and s.category is not null
            )
            order by c.id
            """)
    List<Category> findByMasterId(@Param("masterId") Long masterId);
}

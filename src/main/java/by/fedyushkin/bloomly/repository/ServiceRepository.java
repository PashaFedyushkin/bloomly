package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query(
            value = """
                    select s from Service s
                    left join s.master m
                    left join s.category c
                    where (:masterId is null or m.id = :masterId)
                      and (:categoryName is null or lower(c.name) = lower(:categoryName))
                    """,
            countQuery = """
                    select count(s) from Service s
                    left join s.master m
                    left join s.category c
                    where (:masterId is null or m.id = :masterId)
                      and (:categoryName is null or lower(c.name) = lower(:categoryName))
                    """
    )
    Page<Service> findFiltered(
            @Param("masterId") Long masterId,
            @Param("categoryName") String categoryName,
            Pageable pageable
    );
}

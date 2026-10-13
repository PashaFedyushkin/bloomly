package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    List<Review> findBySessionMasterId(Long masterId);

    Page<Review> findBySessionMasterId(Long masterId, Pageable pageable);

    @Query("""
            select r.stars, count(r)
            from Review r
            where r.session.master.id = :masterId
            group by r.stars
            """)
    List<Object[]> countByStarsForMaster(@Param("masterId") Long masterId);

    @Query("""
            select avg(r.stars)
            from Review r
            where r.session.master.id = :masterId
            """)
    Double averageStarsByMasterId(@Param("masterId") Long masterId);

    List<Review> findBySessionUserId(Long userId);

    Page<Review> findBySessionUserId(Long userId, Pageable pageable);
}

package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    List<Review> findBySessionMasterId(Long masterId);

    Page<Review> findBySessionMasterId(Long masterId, Pageable pageable);

    List<Review> findBySessionUserId(Long userId);

    Page<Review> findBySessionUserId(Long userId, Pageable pageable);
}

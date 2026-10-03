package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    Optional<Portfolio> findByMasterId(Long masterId);

    boolean existsByMasterId(Long masterId);
}

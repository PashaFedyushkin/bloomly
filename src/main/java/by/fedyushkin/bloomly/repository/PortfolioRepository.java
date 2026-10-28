package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByMasterIdOrderByIdAsc(Long masterId);
}

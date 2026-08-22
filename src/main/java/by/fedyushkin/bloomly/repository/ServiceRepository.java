package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByMasterId(Long masterId);
}

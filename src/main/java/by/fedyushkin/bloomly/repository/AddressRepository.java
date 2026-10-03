package by.fedyushkin.bloomly.repository;

import by.fedyushkin.bloomly.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}

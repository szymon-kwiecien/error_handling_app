package pl.error_handling_app.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.error_handling_app.company.entity.Company;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);
}

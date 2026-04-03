package pl.error_handling_app.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.error_handling_app.report.entity.ReportCategory;

import java.util.Optional;

public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Long> {

    boolean existsByName(String name);
    Optional<ReportCategory> findByName(String name);
}

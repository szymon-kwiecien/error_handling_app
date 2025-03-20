package pl.error_handling_app.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Long> {

    boolean existsByName(String name);
}

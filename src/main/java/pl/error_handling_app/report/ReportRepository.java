package pl.error_handling_app.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.error_handling_app.user.User;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByAssignedEmployee(User user);
    List<Report> findAllByReportingUser(User user);
    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);
}

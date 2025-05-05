package pl.error_handling_app.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.error_handling_app.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    List<Report> findAllByAssignedEmployee(User user);
    List<Report> findAllByReportingUser(User user);
    List<Report> findAllByDatedAddedIsBetween(LocalDateTime dateFrom, LocalDateTime dateTo);
    List<Report> findByStatusAndTimeToRespondLessThanEqual(ReportStatus status, LocalDateTime dateTime);
    List<Report> findByStatusAndDueDateLessThanEqual(ReportStatus status, LocalDateTime dateTime);

}

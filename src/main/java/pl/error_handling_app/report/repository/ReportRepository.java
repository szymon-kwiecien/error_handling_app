package pl.error_handling_app.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    List<Report> findAllByAssignedEmployee(User user);
    List<Report> findAllByReportingUser(User user);
    List<Report> findAllByDatedAddedIsBetween(LocalDateTime dateFrom, LocalDateTime dateTo);
    List<Report> findByStatusAndTimeToRespondLessThanEqual(ReportStatus status, LocalDateTime dateTime);
    List<Report> findByStatusAndDueDateLessThanEqual(ReportStatus status, LocalDateTime dateTime);
    @Modifying
    @Query("UPDATE Report r SET r.assignedEmployee = null WHERE r.assignedEmployee.company.id = :companyId")
    void nullifyAssignmentsForCompanyUsers(Long companyId);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.reportingUser.company.id = :companyId")
    void deleteReportsCreatedByCompanyUsers(Long companyId);

}

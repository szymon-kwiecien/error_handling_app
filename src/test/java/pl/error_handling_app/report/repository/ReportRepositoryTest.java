package pl.error_handling_app.report.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    private Company company1;
    private Company company2;
    private User reporter1;
    private User assignee1;
    private User reporter2;
    private User assignee2;
    private Report report1;
    private Report report2;
    private Report report3;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        
        company1 = new Company();
        company1.setName("Firma A");
        company1.setTimeToFirstRespond(24);
        company1.setTimeToResolve(48);
        company1 = entityManager.persist(company1);

        company2 = new Company();
        company2.setName("Firma B");
        company2.setTimeToFirstRespond(12);
        company2.setTimeToResolve(24);
        company2 = entityManager.persist(company2);
        
        ReportCategory category = new ReportCategory();
        category.setName("Kategoria Testowa");
        category = entityManager.persist(category);
        
        reporter1 = new User();
        reporter1.setFirstName("Jan");
        reporter1.setLastName("Kowalski");
        reporter1.setPassword("haslo123");
        reporter1.setEmail("r1@test.pl");
        reporter1.setCompany(company1);
        reporter1 = entityManager.persist(reporter1);

        assignee1 = new User();
        assignee1.setFirstName("Admin");
        assignee1.setLastName("Fixaro");
        assignee1.setPassword("haslo123");
        assignee1.setEmail("a1@test.pl");
        assignee1.setCompany(company1);
        assignee1 = entityManager.persist(assignee1);

        reporter2 = new User();
        reporter2.setFirstName("Anna");
        reporter2.setLastName("Nowak");
        reporter2.setPassword("haslo123");
        reporter2.setEmail("r2@test.pl");
        reporter2.setCompany(company2);
        reporter2 = entityManager.persist(reporter2);

        assignee2 = new User();
        assignee2.setFirstName("Tech");
        assignee2.setLastName("Master");
        assignee2.setPassword("haslo123");
        assignee2.setEmail("a2@test.pl");
        assignee2.setCompany(company2);
        assignee2 = entityManager.persist(assignee2);

        now = LocalDateTime.now();
        
        report1 = new Report();
        report1.setTitle("Raport 1");
        report1.setDescription("Opis 1");
        report1.setCategory(category);
        report1.setReportingUser(reporter1);
        report1.setAssignedEmployee(assignee1);
        report1.setStatus(ReportStatus.PENDING);
        report1.setDatedAdded(now);
        report1.setDueDate(now.plusDays(2));
        report1.setTimeToRespond(now.plusHours(2));
        report1 = entityManager.persist(report1);
        
        report2 = new Report();
        report2.setTitle("Raport 2");
        report2.setDescription("Opis 2");
        report2.setCategory(category);
        report2.setReportingUser(reporter2);
        report2.setAssignedEmployee(assignee2);
        report2.setStatus(ReportStatus.COMPLETED);
        report2.setDatedAdded(now.minusDays(5));
        report2.setDueDate(now.minusDays(1));
        report2.setTimeToRespond(now.minusHours(5));
        report2 = entityManager.persist(report2);
        
        report3 = new Report();
        report3.setTitle("Raport 3");
        report3.setDescription("Opis 3");
        report3.setCategory(category);
        report3.setReportingUser(reporter1);
        report3.setAssignedEmployee(null); //brak pracownika
        report3.setStatus(ReportStatus.PENDING);
        report3.setDatedAdded(now.plusDays(5));
        report3.setDueDate(now.plusDays(10));
        report3.setTimeToRespond(now.plusHours(10));
        report3 = entityManager.persist(report3);

        entityManager.flush();
    }

    @Test
    void shouldFindAllByAssignedEmployee() {
        //when
        List<Report> results = reportRepository.findAllByAssignedEmployee(assignee1);

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Raport 1");
    }

    @Test
    void shouldFindAllByReportingUser() {
        //when
        List<Report> results = reportRepository.findAllByReportingUser(reporter1);

        //then
        assertThat(results).hasSize(2)
                .extracting(Report::getTitle)
                .containsExactlyInAnyOrder("Raport 1", "Raport 3");
    }
    
    @Test
    void shouldFindByStatusAndTimeToRespondLessThanEqual() {
        //given
        LocalDateTime threshold = now.plusHours(5);

        //when
        List<Report> results = reportRepository.findByStatusAndTimeToRespondLessThanEqual(ReportStatus.PENDING, threshold);

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Raport 1");
    }

    @Test
    void shouldFindByStatusAndDueDateLessThanEqual() {
        //given
        LocalDateTime threshold = now.plusDays(5);

        //when
        List<Report> results = reportRepository.findByStatusAndDueDateLessThanEqual(ReportStatus.PENDING, threshold);

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Raport 1");
    }

    @Test
    void shouldNullifyAssignmentsForCompanyUsers() {
        //when
        reportRepository.nullifyAssignmentsForCompanyUsers(company1.getId());
        
        entityManager.flush();
        entityManager.clear();

        //then
        Report updatedReport1 = reportRepository.findById(report1.getId()).orElseThrow();
        Report unaffectedReport2 = reportRepository.findById(report2.getId()).orElseThrow();

        assertThat(updatedReport1.getAssignedEmployee()).isNull();
        assertThat(unaffectedReport2.getAssignedEmployee()).isNotNull();
    }

    @Test
    void shouldDeleteReportsCreatedByCompanyUsers() {
        //when
        reportRepository.deleteReportsCreatedByCompanyUsers(company1.getId());

        entityManager.flush();
        entityManager.clear();

        //then
        List<Report> remainingReports = reportRepository.findAll();

        assertThat(remainingReports).hasSize(1);
        assertThat(remainingReports.get(0).getTitle()).isEqualTo("Raport 2");
    }
}
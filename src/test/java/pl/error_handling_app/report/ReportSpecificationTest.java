package pl.error_handling_app.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.report.entity.Report;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.repository.ReportRepository;
import pl.error_handling_app.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReportSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    private User reporter1;
    private User reporter2;
    private User assignee1;

    private Report report1;
    private Report report2;
    private Report report3;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();

        Company existingCompany = entityManager.getEntityManager()
                .createQuery("SELECT c FROM Company c", Company.class)
                .setMaxResults(1)
                .getSingleResult();

        ReportCategory category = new ReportCategory();
        category.setName("Testowa Kategoria");
        entityManager.persist(category);

        reporter1 = new User();
        reporter1.setFirstName("Jan");
        reporter1.setLastName("Kowalski");
        reporter1.setPassword("zaq1@WSX");
        reporter1.setEmail("reporter1_test@fixaro.pl");
        reporter1.setCompany(existingCompany);
        entityManager.persist(reporter1);

        reporter2 = new User();
        reporter2.setFirstName("Anna");
        reporter2.setLastName("Nowak");
        reporter2.setPassword("zaq1@WSX");
        reporter2.setEmail("reporter2_test@fixaro.pl");
        reporter2.setCompany(existingCompany);
        entityManager.persist(reporter2);

        assignee1 = new User();
        assignee1.setFirstName("Admin");
        assignee1.setLastName("Fixaro");
        assignee1.setPassword("zaq1@WSX");
        assignee1.setEmail("admin_test@fixaro.pl");
        assignee1.setCompany(existingCompany);
        entityManager.persist(assignee1);

        LocalDateTime now = java.time.LocalDateTime.now();

        report1 = new Report();
        report1.setTitle("Błąd logowania do systemu");
        report1.setDescription("Użytkownik nie może się zalogować przy użyciu poprawnego hasła.");
        report1.setDatedAdded(now);
        report1.setDueDate(now.plusDays(7));
        report1.setTimeToRespond(now.plusHours(24));
        report1.setCategory(category);
        report1.setStatus(ReportStatus.PENDING);
        report1.setReportingUser(reporter1);
        report1.setAssignedEmployee(assignee1);
        entityManager.persist(report1);

        report2 = new Report();
        report2.setTitle("Awaria modułu płatności");
        report2.setDescription("Bramka płatnicza zwraca błąd 500 w momencie finalizacji.");
        report2.setDatedAdded(now);
        report2.setDueDate(now.plusDays(3));
        report2.setTimeToRespond(now.plusHours(12));
        report2.setCategory(category);
        report2.setStatus(ReportStatus.PENDING);
        report2.setReportingUser(reporter1);
        entityManager.persist(report2);

        report3 = new Report();
        report3.setTitle("Brakujący raport w systemie");
        report3.setDescription("Po wygenerowaniu raportu PDF, plik nie pobiera się na dysk.");
        report3.setDatedAdded(now);
        report3.setDueDate(now.plusDays(5));
        report3.setTimeToRespond(now.plusHours(48));
        report3.setCategory(category);
        report3.setStatus(ReportStatus.COMPLETED);
        report3.setReportingUser(reporter2);
        report3.setAssignedEmployee(assignee1);
        entityManager.persist(report3);

        entityManager.flush();
    }

    @Test
    void shouldFilterByTitleFragmentCaseInsensitive() {
        //given
        Specification<Report> spec = ReportSpecification.filterBy("błąd", null);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Błąd logowania do systemu");
    }

    @Test
    void shouldFilterByTitleFragmentMatchingMultiple() {
        //given
        Specification<Report> spec = ReportSpecification.filterBy("system", null);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).hasSize(2)
                .extracting(Report::getTitle)
                .containsExactlyInAnyOrder("Błąd logowania do systemu", "Brakujący raport w systemie");
    }

    @Test
    void shouldFilterByStatusOnly() {
        //given
        Specification<Report> spec = ReportSpecification.filterBy(null, ReportStatus.PENDING);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).hasSize(2)
                .extracting(Report::getTitle)
                .containsExactlyInAnyOrder("Błąd logowania do systemu", "Awaria modułu płatności");
    }

    @Test
    void shouldFilterByBothTitleAndStatus() {
        //given
        Specification<Report> spec = ReportSpecification.filterBy("awaria", ReportStatus.PENDING);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Awaria modułu płatności");
    }

    @Test
    void shouldReturnEmptyWhenCriteriaDoNotMatch() {
        //given
        Specification<Report> spec = ReportSpecification.filterBy("raport", ReportStatus.PENDING);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnAllWhenFiltersAreNullOrEmpty() {
        //given
        Specification<Report> spec = ReportSpecification.filterBy("", null);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).hasSize(3);
    }

    @Test
    void shouldFilterByAssignedEmployee() {
        //given
        Specification<Report> spec = ReportSpecification.filterByAssignedEmployee(assignee1);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).hasSize(2)
                .extracting(Report::getTitle)
                .containsExactlyInAnyOrder("Błąd logowania do systemu", "Brakujący raport w systemie");
    }

    @Test
    void shouldFilterByReportingUser() {
        //given
        Specification<Report> spec = ReportSpecification.filterByReportingUser(reporter1);

        //when
        List<Report> results = reportRepository.findAll(spec);

        //then
        assertThat(results).hasSize(2)
                .extracting(Report::getTitle)
                .containsExactlyInAnyOrder("Błąd logowania do systemu", "Awaria modułu płatności");
    }
}
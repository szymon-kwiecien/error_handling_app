package pl.error_handling_app.summary.helper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.context.Context;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.entity.ReportCategory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SummaryContextBuilderTest {

    @BeforeEach
    void setUp() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin@firma.pl");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBuildContextForAllUsersWithNullCategoryAndStatus() {
        //given
        LocalDate from = LocalDate.of(2023, 1, 1);
        LocalDate to = LocalDate.of(2023, 12, 31);
        String user = "all";
        String sortedBy = "dateAdded,desc";

        //when
        Context context = SummaryContextBuilder.build(from, to, null, user, List.of(), null, sortedBy);

        //then
        assertThat(context.getVariable("summaryHeader")).isEqualTo("Raport dotyczący zgłoszeń");

        assertThat(context.getVariable("categories")).isEqualTo("Wszystkie");
        assertThat(context.getVariable("status")).isEqualTo("Wszystkie");

        assertThat(context.getVariable("dateRange")).isEqualTo("2023-01-01 - 2023-12-31");
        assertThat(context.getVariable("sort")).isEqualTo(sortedBy);
        assertThat(context.getVariable("currentUserName")).isEqualTo("admin@firma.pl");
        assertThat(context.getVariable("currentDate")).isNotNull();
        assertThat(context.getVariable("reports")).isEqualTo(List.of());
    }

    @Test
    void shouldBuildContextForSpecificUserWithProvidedCategoryAndStatus() {
        //given
        LocalDate from = LocalDate.of(2023, 6, 1);
        LocalDate to = LocalDate.of(2023, 6, 30);
        String user = "jan@kowalski.pl";
        String sortedBy = "status,asc";

        ReportCategory category = mock(ReportCategory.class);
        when(category.getName()).thenReturn("Problemy sprzętowe");

        ReportStatus status = ReportStatus.values()[0];

        //when
        Context context = SummaryContextBuilder.build(from, to, status, user, List.of(), category, sortedBy);

        //then
        assertThat(context.getVariable("summaryHeader")).isEqualTo("Raport dotyczący pracownika jan@kowalski.pl");

        assertThat(context.getVariable("categories")).isEqualTo("Problemy sprzętowe");
        assertThat(context.getVariable("status")).isEqualTo(status.polishName);
    }

    @Test
    void shouldFormatDatesProperlyForReportsMap() {
        // given
        ReportDto report1 = mock(ReportDto.class);
        when(report1.getId()).thenReturn(10L);
        when(report1.getDateAdded()).thenReturn(LocalDateTime.of(2023, 5, 10, 14, 30, 45));

        ReportDto report2 = mock(ReportDto.class);
        when(report2.getId()).thenReturn(20L);
        when(report2.getDateAdded()).thenReturn(LocalDateTime.of(2023, 11, 5, 9, 5, 10));

        //when
        Context context = SummaryContextBuilder.build(
                LocalDate.now(), LocalDate.now(), null, "all", List.of(report1, report2), null, "id,asc"
        );

        //then
        @SuppressWarnings("unchecked")
        Map<Long, String> formattedDates = (Map<Long, String>) context.getVariable("formattedDates");

        assertThat(formattedDates)
                .containsEntry(10L, "2023-05-10 14:30")
                .containsEntry(20L, "2023-11-05 09:05");
    }
}
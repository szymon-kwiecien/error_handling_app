package pl.error_handling_app.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pl.error_handling_app.report.dto.ReportDto;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailUtilsTest {

    private final EmailUtils emailUtils = new EmailUtils();

    @ParameterizedTest(name = "Dla emaila ''{0}'' oczekiwana część lokalna to ''{1}'', a domena to ''{2}''")
    @CsvSource({
            "jan.kowalski@example.com, jan.kowalski, @example.com",
            "admin@firma.pl, admin, @firma.pl",
            "brak_malpy_w_adresie, brak_malpy_w_adresie, ''",
            "'', '', ''" // pusty mail
    })
    void shouldSplitEmailCorrectly(String email, String expectedLocalPart, String expectedDomain) {
        //when
        Map<String, String> result = EmailUtils.splitEmail(email);

        //then
        String domainToAssert = (expectedDomain == null) ? "" : expectedDomain;

        assertThat(result)
                .containsEntry("localPart", expectedLocalPart)
                .containsEntry("domain", domainToAssert);
    }

    @Test
    void shouldHandleNullEmailInSplitEmail() {
        //when
        Map<String, String> result = EmailUtils.splitEmail(null);

        //then
        assertThat(result)
                .containsEntry("localPart", "")
                .containsEntry("domain", "");
    }

    @Test
    void shouldExtractEmailPartsFromReportList() {
        //given
        ReportDto report1 = mock(ReportDto.class);
        when(report1.getId()).thenReturn(1L);
        when(report1.getReportingUser()).thenReturn("zglaszajacy@domena.pl");
        when(report1.getAssignedEmployee()).thenReturn("pracownik@firma.com");

        ReportDto report2 = mock(ReportDto.class);
        when(report2.getId()).thenReturn(2L);
        when(report2.getReportingUser()).thenReturn("inny_zglaszajacy"); //brak @
        when(report2.getAssignedEmployee()).thenReturn(null);

        //when
        EmailUtils.EmailPartsMap result = emailUtils.extractEmailParts(List.of(report1, report2));

        //then
        assertThat(result.reportingLocalPart())
                .containsEntry(1L, "zglaszajacy")
                .containsEntry(2L, "inny_zglaszajacy");

        assertThat(result.reportingDomain())
                .containsEntry(1L, "@domena.pl")
                .containsEntry(2L, "");

        assertThat(result.assignedLocalPart())
                .containsEntry(1L, "pracownik")
                .doesNotContainKey(2L);

        assertThat(result.assignedDomain())
                .containsEntry(1L, "@firma.com")
                .doesNotContainKey(2L);
    }

    @Test
    void shouldReturnEmptyMapsWhenReportListIsEmpty() {
        //given
        List<ReportDto> emptyList = List.of();

        //when
        EmailUtils.EmailPartsMap result = emailUtils.extractEmailParts(emptyList);

        //then
        assertThat(result.reportingLocalPart()).isEmpty();
        assertThat(result.reportingDomain()).isEmpty();
        assertThat(result.assignedLocalPart()).isEmpty();
        assertThat(result.assignedDomain()).isEmpty();
    }
}
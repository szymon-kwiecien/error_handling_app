package pl.error_handling_app.summary.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.service.ReportCategoryService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChartServiceTest {

    @Mock
    private ReportCategoryService reportCategoryService;

    @InjectMocks
    private ChartService chartService;

    @Test
    void shouldGenerateDistributionChartByCategory() throws IOException {
        //given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 1);
        boolean byCategory = true;

        ReportCategoryDto categoryMock = mock(ReportCategoryDto.class);
        when(categoryMock.name()).thenReturn("Sprzęt IT");
        when(reportCategoryService.getAllCategories()).thenReturn(List.of(categoryMock));

        ReportDto reportMock = mock(ReportDto.class);
        when(reportMock.getDateAdded()).thenReturn(LocalDateTime.of(2026, 3, 15, 10, 0));
        when(reportMock.getCategoryName()).thenReturn("Sprzęt IT");

        List<ReportDto> reports = List.of(reportMock);

        //when
        String result = chartService.generateDistributionChart(reports, startDate, endDate, byCategory);

        //then
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void shouldGenerateDistributionChartByStatus() throws IOException {
        //given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 1);
        boolean byCategory = false;

        ReportDto reportMock = mock(ReportDto.class);
        when(reportMock.getDateAdded()).thenReturn(LocalDateTime.of(2026, 2, 10, 10, 0));
        when(reportMock.getStatusName()).thenReturn("Zakończone");

        List<ReportDto> reports = List.of(reportMock);

        //when
        String result = chartService.generateDistributionChart(reports, startDate, endDate, byCategory);

        //then
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void shouldAdjustStartDateIfDifferenceIsMoreThan8MonthsInDistributionChart() throws IOException {
        //given
        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 1);
        boolean byCategory = false;
        List<ReportDto> reports = Collections.emptyList();

        //when
        String result = chartService.generateDistributionChart(reports, startDate, endDate, byCategory);

        //then
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void shouldGenerateAverageTimeComparisonChartForSingleEmployee() throws IOException {
        //given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 1);
        boolean singleEmployee = true;

        Map<LocalDate, Double> firstReactionTimes = Map.of(
                LocalDate.of(2026, 1, 1), 2.55,
                LocalDate.of(2026, 2, 1), 3.12
        );
        Map<LocalDate, Double> completionTimes = Map.of(
                LocalDate.of(2026, 1, 1), 10.21,
                LocalDate.of(2026, 2, 1), 8.58
        );

        //when
        String result = chartService.generateAverageTimeComparisonChart(
                firstReactionTimes, completionTimes, startDate, endDate, singleEmployee);

        //then
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void shouldGenerateAverageTimeComparisonChartForAllEmployees() throws IOException {
        //given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 2, 1);
        boolean singleEmployee = false;

        Map<LocalDate, Double> firstReactionTimes = Map.of(
                LocalDate.of(2026, 1, 1), 1.5
        );
        Map<LocalDate, Double> completionTimes = Map.of(
                LocalDate.of(2026, 1, 1), 5.0
        );

        //when
        String result = chartService.generateAverageTimeComparisonChart(
                firstReactionTimes, completionTimes, startDate, endDate, singleEmployee);

        //then
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void shouldGenerateAverageTimeChartWithMissingDataMaps() throws IOException {
        //given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 1);
        boolean singleEmployee = false;
        Map<LocalDate, Double> firstReactionTimes = Collections.emptyMap();
        Map<LocalDate, Double> completionTimes = Collections.emptyMap();

        //when
        String result = chartService.generateAverageTimeComparisonChart(
                firstReactionTimes, completionTimes, startDate, endDate, singleEmployee);

        //then
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }
}
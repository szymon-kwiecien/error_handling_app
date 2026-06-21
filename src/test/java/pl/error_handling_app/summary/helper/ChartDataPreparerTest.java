package pl.error_handling_app.summary.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.service.ReportService;
import pl.error_handling_app.summary.service.ChartService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChartDataPreparerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private ChartService chartService;

    @InjectMocks
    private ChartDataPreparer chartDataPreparer;

    @Test
    void shouldPrepareChartsForAllEmployees() throws IOException {
        //given
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 5, 31);
        List<ReportDto> reports = List.of(mock(ReportDto.class));
        List<ReportDto> allFoundReports = List.of();
        boolean isSingleEmployee = false;

        Map<LocalDate, Double> firstReactionTimes = Map.of(dateFrom, 2.5);
        Map<LocalDate, Double> completionTimes = Map.of(dateFrom, 5.0);

        when(reportService.getAverageFirstReactionTimesForReports(reports, dateFrom, dateTo))
                .thenReturn(firstReactionTimes);
        when(reportService.getAverageCompletionTimesForReports(reports, dateFrom, dateTo))
                .thenReturn(completionTimes);

        when(chartService.generateDistributionChart(reports, dateFrom, dateTo, true)).thenReturn("categoryChartBase64");
        when(chartService.generateDistributionChart(reports, dateFrom, dateTo, false)).thenReturn("statusChartBase64");
        when(chartService.generateAverageTimeComparisonChart(firstReactionTimes, completionTimes, dateFrom, dateTo, isSingleEmployee))
                .thenReturn("averageTimesChartBase64");

        //when
        Map<String, Object> result = chartDataPreparer.prepareCharts(reports, allFoundReports, dateFrom, dateTo, isSingleEmployee);

        //then
        assertThat(result).hasSize(3);
        assertThat(result.get("categoryChart")).isEqualTo("categoryChartBase64");
        assertThat(result.get("statusChart")).isEqualTo("statusChartBase64");
        assertThat(result.get("averageCompletionTimeChart")).isEqualTo("averageTimesChartBase64");

        verify(reportService, never()).getAverageCompletionTimesForReports(allFoundReports, dateFrom, dateTo);
    }

    @Test
    void shouldPrepareChartsForSingleEmployeeAndOverrideFirstReactionTimes() throws IOException {
        //given
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 5, 31);
        List<ReportDto> employeeReports = List.of(mock(ReportDto.class));
        List<ReportDto> allFoundReports = List.of(mock(ReportDto.class), mock(ReportDto.class));
        boolean isSingleEmployee = true;

        Map<LocalDate, Double> employeeReactionTimes = Map.of(dateFrom, 1.0);
        Map<LocalDate, Double> employeeCompletionTimes = Map.of(dateFrom, 4.0);
        Map<LocalDate, Double> allUsersCompletionTimes = Map.of(dateFrom, 6.0);

        when(reportService.getAverageFirstReactionTimesForReports(employeeReports, dateFrom, dateTo))
                .thenReturn(employeeReactionTimes);
        when(reportService.getAverageCompletionTimesForReports(employeeReports, dateFrom, dateTo))
                .thenReturn(employeeCompletionTimes);

        when(reportService.getAverageCompletionTimesForReports(allFoundReports, dateFrom, dateTo))
                .thenReturn(allUsersCompletionTimes);

        when(chartService.generateAverageTimeComparisonChart(allUsersCompletionTimes, employeeCompletionTimes, dateFrom, dateTo, isSingleEmployee))
                .thenReturn("averageTimesChartBase64");

        //when
        Map<String, Object> result = chartDataPreparer.prepareCharts(employeeReports, allFoundReports, dateFrom, dateTo, isSingleEmployee);

        //then
        assertThat(result.get("averageCompletionTimeChart")).isEqualTo("averageTimesChartBase64");
        verify(reportService).getAverageCompletionTimesForReports(allFoundReports, dateFrom, dateTo);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenChartServiceThrowsIOException() throws IOException {
        //given
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 5, 31);
        List<ReportDto> reports = List.of();

        when(chartService.generateDistributionChart(reports, dateFrom, dateTo, true))
                .thenThrow(new IOException("Błąd I/O biblioteki JFreeChart"));

        //when, then
        assertThatThrownBy(() -> chartDataPreparer.prepareCharts(reports, List.of(), dateFrom, dateTo, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Błąd podczas generowania wykresów")
                .hasCauseInstanceOf(IOException.class);
    }
}
package pl.error_handling_app.summary;

import org.springframework.stereotype.Service;
import pl.error_handling_app.report.ReportService;
import pl.error_handling_app.report.dto.ReportDto;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChartDataPreparer {

    private final ReportService reportService;
    private final ChartService chartService;

    public ChartDataPreparer(ReportService reportService, ChartService chartService) {
        this.reportService = reportService;
        this.chartService = chartService;
    }

    public Map<String, Object> prepareCharts(
            List<ReportDto> reports,
            List<ReportDto> allFoundReports,
            LocalDate dateFrom,
            LocalDate dateTo,
            boolean isSingleEmployee
    ) {
        Map<String, Object> chartData = new HashMap<>();

        Map<LocalDate, Double> firstReactionTimes = reportService.getAverageFirstReactionTimesForReports(reports, dateFrom, dateTo);
        Map<LocalDate, Double> completionTimes = reportService.getAverageCompletionTimesForReports(reports, dateFrom, dateTo);

        if (isSingleEmployee) {
            firstReactionTimes = reportService.getAverageCompletionTimesForReports(allFoundReports, dateFrom, dateTo);
        }

        try {
            String categoryChart = chartService.generateDistributionChart(reports, dateFrom, dateTo, true);
            String statusChart = chartService.generateDistributionChart(reports, dateFrom, dateTo, false);
            String avgTimesChartData = chartService.generateAverageTimeComparisonChart(firstReactionTimes, completionTimes, dateFrom, dateTo, isSingleEmployee);

            chartData.put("categoryChart", categoryChart);
            chartData.put("statusChart", statusChart);
            chartData.put("averageCompletionTimeChart", avgTimesChartData);
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas generowania wykresów", e);
        }

        return chartData;
    }
}

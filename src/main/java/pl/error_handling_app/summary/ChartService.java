package pl.error_handling_app.summary;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import pl.error_handling_app.report.*;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.dto.ReportDto;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChartService {

    private static final String[] POLISH_MONTHS_NAMES = {
            "styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec",
            "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień"
    };
    private final ReportCategoryService reportCategoryService;

    public ChartService(ReportCategoryService reportCategoryService) {
        this.reportCategoryService = reportCategoryService;
    }

    public String generateDistributionChart(List<ReportDto> reports, LocalDate startDate, LocalDate endDate, boolean byCategory) throws IOException {
        // limit maksymalnie 8 miesięcy
        final LocalDate adjustedStartDate = startDate.isAfter(endDate.minusMonths(7).withDayOfMonth(1))
                ? startDate.withDayOfMonth(1)
                : endDate.minusMonths(7).withDayOfMonth(1);

        // Lista nazw kategorii lub statusów
        List<String> values = byCategory
                ? reportCategoryService.getAllCategories().stream()
                .map(ReportCategoryDto::getName)
                .toList()
                : Arrays.stream(ReportStatus.values())
                .map(rs -> rs.description)
                .toList();

        Map<LocalDate, Map<String, Long>> groupedData = new LinkedHashMap<>();
        adjustedStartDate.datesUntil(endDate.withDayOfMonth(1).plusMonths(1), Period.ofMonths(1))
                .forEach(date -> {
                    Map<String, Long> dataMap = values.stream().collect(Collectors.toMap(
                            value -> value, value -> 0L));
                    groupedData.put(date, dataMap);
                });

        reports.stream()
                .filter(report -> !report.getDateAdded().toLocalDate().isBefore(adjustedStartDate) &&
                        !report.getDateAdded().toLocalDate().isAfter(endDate))
                .forEach(report -> {
                    LocalDate month = report.getDateAdded().toLocalDate().with(TemporalAdjusters.firstDayOfMonth());
                    Map<String, Long> existingCounts = groupedData.get(month);
                    String key = byCategory ? report.getCategoryName() : report.getStatusName();
                    existingCounts.put(key, existingCounts.getOrDefault(key, 0L) + 1);
                });

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        groupedData.forEach((date, dataCounts) -> {
            String monthName = POLISH_MONTHS_NAMES[date.getMonthValue() - 1] + " " + date.getYear();
            values.forEach(value -> {
                long count = dataCounts.getOrDefault(value, 0L);
                dataset.addValue(count, value, monthName);
            });
        });

        String title = byCategory ? "Liczba zgłoszeń według kategorii" : "Liczba zgłoszeń według statusów";
        JFreeChart chart = ChartFactory.createStackedBarChart(
                title,
                "Miesiąc",
                "Liczba zgłoszeń",
                dataset
        );

        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        chart.getCategoryPlot().getDomainAxis().setLabelFont(new Font("Arial", Font.PLAIN, 12));
        chart.getCategoryPlot().getRangeAxis().setLabelFont(new Font("Arial", Font.PLAIN, 12));
        StackedBarRenderer renderer = createRenderer();
        chart.getCategoryPlot().setRenderer(renderer);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 500);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private static StackedBarRenderer createRenderer() {
        StackedBarRenderer renderer = new StackedBarRenderer();
        renderer.setSeriesPaint(0, new Color(244, 67, 54)); // oczekujące - czerwony
        renderer.setSeriesPaint(1, new Color(33, 150, 243)); // w trakcei - niebieski
        renderer.setSeriesPaint(2, new Color(76, 175, 80)); // zakonczone - zielony

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Arial", Font.BOLD, 10));
        return renderer;
    }

    public String generateAverageTimeComparisonChart(Map<LocalDate, Double> firstReactionTimes, Map<LocalDate, Double> completionTimes,
                                                     LocalDate startDate, LocalDate endDate, boolean singleEmployee) throws IOException {
        final LocalDate adjustedStartDate = startDate.isAfter(endDate.minusMonths(7).withDayOfMonth(1))
                ? startDate.withDayOfMonth(1)
                : endDate.minusMonths(7).withDayOfMonth(1);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String firstLabelName = singleEmployee
                ? "Średni czas obsługi zgłoszeń przez wszystkich pracowników (godziny)"
                : "Średni czas pierwszej reakcji na zgłoszenie (godziny)";

        LocalDate current = adjustedStartDate;
        LocalDate last = endDate.withDayOfMonth(1);

        while (!current.isAfter(last)) {
            int monthIndex = current.getMonthValue() - 1;
            String monthName = POLISH_MONTHS_NAMES[monthIndex] + " " + current.getYear();

            Double averageFirstReactionTime = firstReactionTimes.getOrDefault(current, 0.0);
            Double averageCompletionTime = completionTimes.getOrDefault(current, 0.0);

            averageFirstReactionTime = Math.round(averageFirstReactionTime * 10.0) / 10.0;
            averageCompletionTime = Math.round(averageCompletionTime * 10.0) / 10.0;

            dataset.addValue(averageFirstReactionTime, firstLabelName, monthName);
            dataset.addValue(averageCompletionTime, "Średni czas obsługi zgłoszeń pracownika (godziny)", monthName);

            current = current.plusMonths(1);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Porównanie średnich czasów obsługi zgłoszeń",
                "Miesiąc",
                "Czas (godziny)",
                dataset
        );

        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
        chart.getCategoryPlot().getDomainAxis().setLabelFont(new Font("Arial", Font.PLAIN, 12));
        chart.getCategoryPlot().getRangeAxis().setLabelFont(new Font("Arial", Font.PLAIN, 12));

        BarRenderer renderer = new BarRenderer();
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Arial", Font.BOLD, 10));

        renderer.setSeriesPaint(0, new Color(79, 129, 189)); // pierwsza reakcja -niebieski
        renderer.setSeriesPaint(1, new Color(192, 80, 77));  // obsługa zlgoszenia - czerwony

        chart.getCategoryPlot().setRenderer(renderer);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(outputStream, chart, 800, 500);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}

package pl.error_handling_app.report.service;

import org.springframework.stereotype.Service;
import pl.error_handling_app.report.dto.ReportDto;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportStatisticsService {

    public Map<LocalDate, Double> calculateAverageTimes(List<ReportDto> reports, LocalDate startDate, LocalDate endDate, Function<ReportDto, Double> timeExtractor) {
        if (reports == null || reports.isEmpty()) {
            return Map.of();
        }

        Map<LocalDate, List<Double>> groupedTimes = startDate.withDayOfMonth(1)
                .datesUntil(endDate.withDayOfMonth(1).plusMonths(1), Period.ofMonths(1))
                .collect(Collectors.toMap(
                        date -> date,
                        date -> new ArrayList<>()
                ));

        reports.stream()
                .filter(report -> !report.getDateAdded().toLocalDate().isBefore(startDate) &&
                        !report.getDateAdded().toLocalDate().isAfter(endDate))
                .forEach(report -> {
                    LocalDate month = report.getDateAdded().toLocalDate().with(TemporalAdjusters.firstDayOfMonth());
                    Double duration = timeExtractor.apply(report);

                    if (duration != null && groupedTimes.containsKey(month)) {
                        groupedTimes.get(month).add(duration);
                    }
                });

        return groupedTimes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0)
                ));
    }
}
package pl.error_handling_app.report.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.error_handling_app.report.dto.ReportDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportStatisticsServiceTest {

    private ReportStatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        statisticsService = new ReportStatisticsService();
    }

    @Test
    void shouldReturnEmptyMapWhenReportsListIsNull() {
        //when
        Map<LocalDate, Double> result = statisticsService.calculateAverageTimes(null, LocalDate.now(), LocalDate.now(), r -> 10.0);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyMapWhenReportsListIsEmpty() {
        //when
        Map<LocalDate, Double> result = statisticsService.calculateAverageTimes(List.of(), LocalDate.now(), LocalDate.now(), r -> 10.0);

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCalculateAverageTimeCorrectlyForSingleMonth() {
        //given
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);

        ReportDto report1 = mockReportDto(LocalDateTime.of(2026, 5, 10, 12, 0), 10.0);
        ReportDto report2 = mockReportDto(LocalDateTime.of(2026, 5, 20, 12, 0), 20.0);

        Function<ReportDto, Double> extractor = ReportDto::getAddedToCompleteDuration;

        //when
        Map<LocalDate, Double> result = statisticsService.calculateAverageTimes(List.of(report1, report2), startDate, endDate, extractor);

        //then
        LocalDate expectedMonthKey = LocalDate.of(2026, 5, 1);
        assertThat(result).hasSize(1);
        assertThat(result).containsKey(expectedMonthKey);
        assertThat(result.get(expectedMonthKey)).isEqualTo(15.0);
    }

    @Test
    void shouldIncludeMonthsWithZeroAverageWhenNoReportsExistInThatMonth() {
        //given
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 7, 31);

        ReportDto reportMay = mockReportDto(LocalDateTime.of(2026, 5, 15, 12, 0), 10.0);
        ReportDto reportJuly = mockReportDto(LocalDateTime.of(2026, 7, 15, 12, 0), 30.0);

        //when
        Map<LocalDate, Double> result = statisticsService.calculateAverageTimes(List.of(reportMay, reportJuly), startDate, endDate, ReportDto::getAddedToCompleteDuration);

        //then
        assertThat(result).hasSize(3); //maj, czerwiec, lipiec
        assertThat(result.get(LocalDate.of(2026, 5, 1))).isEqualTo(10.0);
        assertThat(result.get(LocalDate.of(2026, 6, 1))).isEqualTo(0.0); //srednia 0.0
        assertThat(result.get(LocalDate.of(2026, 7, 1))).isEqualTo(30.0);
    }

    @Test
    void shouldIgnoreReportsOutsideOfDateRange() {
        //given
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        ReportDto reportBefore = mockReportDto(LocalDateTime.of(2026, 5, 31, 23, 59), 10.0);
        ReportDto reportInside = mockReportDto(LocalDateTime.of(2026, 6, 15, 12, 0), 20.0);
        ReportDto reportAfter = mockReportDto(LocalDateTime.of(2026, 7, 1, 0, 1), 30.0);

        //when
        Map<LocalDate, Double> result = statisticsService.calculateAverageTimes(
                List.of(reportBefore, reportInside, reportAfter), startDate, endDate, ReportDto::getAddedToCompleteDuration);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(LocalDate.of(2026, 6, 1))).isEqualTo(20.0);
    }

    @Test
    void shouldIgnoreReportsWithNullExtractedDuration() {
        //given
        LocalDate startDate = LocalDate.of(2026, 5, 1);
        LocalDate endDate = LocalDate.of(2026, 5, 31);

        ReportDto reportValid = mockReportDto(LocalDateTime.of(2026, 5, 10, 12, 0), 10.0);
        ReportDto reportWithNullTime = mockReportDto(LocalDateTime.of(2026, 5, 15, 12, 0), null);

        //when
        Map<LocalDate, Double> result = statisticsService.calculateAverageTimes(
                List.of(reportValid, reportWithNullTime), startDate, endDate, ReportDto::getAddedToCompleteDuration);

        //then
        assertThat(result.get(LocalDate.of(2026, 5, 1))).isEqualTo(10.0);
    }

    private ReportDto mockReportDto(LocalDateTime dateAdded, Double durationToReturn) {
        ReportDto mockDto = mock(ReportDto.class);
        when(mockDto.getDateAdded()).thenReturn(dateAdded);
        when(mockDto.getAddedToCompleteDuration()).thenReturn(durationToReturn);
        return mockDto;
    }
}
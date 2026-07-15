package pl.error_handling_app.summary.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.entity.ReportCategory;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.service.ReportCategoryService;
import pl.error_handling_app.report.service.ReportService;
import pl.error_handling_app.summary.dto.SummaryFormRequest;
import pl.error_handling_app.summary.helper.ChartDataPreparer;
import pl.error_handling_app.summary.helper.SummaryContextBuilder;
import pl.error_handling_app.utils.DateUtils;
import pl.error_handling_app.utils.EmailUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryDataServiceTest {

    @Mock
    private ReportService reportService;

    @Mock
    private ReportFilterService reportFilterService;

    @Mock
    private ChartDataPreparer chartDataPreparer;

    @Mock
    private ReportCategoryService reportCategoryService;

    @Mock
    private EmailUtils emailUtils;

    @InjectMocks
    private SummaryDataService summaryDataService;

    @Test
    void shouldPrepareContextWithTableAndChartsForSingleEmployeeAndExistingCategory() {
        //given
        SummaryFormRequest request = mock(SummaryFormRequest.class);
        when(request.categoryName()).thenReturn("Sprzęt IT");
        when(request.user()).thenReturn("j.kowalski");
        when(request.showReportsTable()).thenReturn(true);
        when(request.showCharts()).thenReturn(true);
        when(request.dateFrom()).thenReturn(LocalDate.of(2026, 1, 1));
        when(request.dateTo()).thenReturn(LocalDate.of(2026, 12, 31));
        when(request.sort()).thenReturn("ASC");
        when(request.status()).thenReturn(ReportStatus.UNDER_REVIEW);

        ReportCategory category = mock(ReportCategory.class);
        when(reportCategoryService.getCategoryByName("Sprzęt IT")).thenReturn(Optional.of(category));

        LocalDate[] parsedDates = {LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)};
        List<ReportDto> reports = List.of(mock(ReportDto.class));

        when(reportFilterService.prepareFilteredReports(request, category, parsedDates[0], parsedDates[1])).thenReturn(reports);
        when(reportService.sortReports(reports, "ASC")).thenReturn("Data rosnąco");

        EmailUtils.EmailPartsMap emailParts = mock(EmailUtils.EmailPartsMap.class);
        when(emailParts.reportingLocalPart()).thenReturn(Collections.emptyMap());
        when(emailParts.reportingDomain()).thenReturn(Collections.emptyMap());
        when(emailParts.assignedLocalPart()).thenReturn(Collections.emptyMap());
        when(emailParts.assignedDomain()).thenReturn(Collections.emptyMap());
        when(emailUtils.extractEmailParts(reports)).thenReturn(emailParts);

        when(reportService.calculateRemainingTime(reports)).thenReturn(new String[0]);

        Map<String, Object> chartData = Map.of(
                "categoryChart", "categoryChartBase64",
                "statusChart", "statusChartBase64",
                "averageCompletionTimeChart", "averageChartBase64"
        );
        when(chartDataPreparer.prepareCharts(reports, reports, parsedDates[0], parsedDates[1], true)).thenReturn(chartData);

        Context expectedContext = new Context();

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<SummaryContextBuilder> contextBuilderMock = mockStatic(SummaryContextBuilder.class)) {

            dateUtilsMock.when(() -> DateUtils.checkAnyDateIsNull(request.dateFrom(), request.dateTo())).thenReturn(parsedDates);
            contextBuilderMock.when(() -> SummaryContextBuilder.build(parsedDates[0], parsedDates[1], ReportStatus.UNDER_REVIEW,
                            "j.kowalski", reports, category, "Data rosnąco"))
                    .thenReturn(expectedContext);

            //when
            Context result = summaryDataService.prepareSummaryContext(request);

            //then
            assertNotNull(result);
            assertEquals(expectedContext, result);
            
            assertTrue((Boolean) result.getVariable("showReportsTable"));
            assertNotNull(result.getVariable("remainingTime"));
            
            assertTrue((Boolean) result.getVariable("showCharts"));
            assertEquals("categoryChartBase64", result.getVariable("categoryReportChart"));
            assertEquals("statusChartBase64", result.getVariable("statusReportChart"));
            assertEquals("averageChartBase64", result.getVariable("averageCompletionTimeChart"));

            verify(emailUtils, times(1)).extractEmailParts(reports);
            verify(chartDataPreparer, times(1)).prepareCharts(reports, reports, parsedDates[0], parsedDates[1], true);
        }
    }

    @Test
    void shouldPrepareContextWithoutTableAndChartsForAllEmployeesAndMissingCategory() {
        //given
        SummaryFormRequest request = mock(SummaryFormRequest.class);
        when(request.categoryName()).thenReturn("Nieistniejąca kategoria");
        when(request.user()).thenReturn("all");
        when(request.showReportsTable()).thenReturn(false);
        when(request.showCharts()).thenReturn(false);
        when(request.dateFrom()).thenReturn(null);
        when(request.dateTo()).thenReturn(null);
        when(request.sort()).thenReturn("DESC");
        when(request.status()).thenReturn(ReportStatus.COMPLETED);

        when(reportCategoryService.getCategoryByName("Nieistniejąca kategoria")).thenReturn(Optional.empty());

        LocalDate fallbackFrom = LocalDate.of(2026, 1, 1);
        LocalDate fallbackTo = LocalDate.of(2026, 1, 31);
        LocalDate[] parsedDates = {fallbackFrom, fallbackTo};
        List<ReportDto> reports = Collections.emptyList();

        when(reportFilterService.prepareFilteredReports(request, null, parsedDates[0], parsedDates[1])).thenReturn(reports);
        when(reportService.sortReports(reports, "DESC")).thenReturn("Data malejąco");

        Context expectedContext = new Context();

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<SummaryContextBuilder> contextBuilderMock = mockStatic(SummaryContextBuilder.class)) {

            dateUtilsMock.when(() -> DateUtils.checkAnyDateIsNull(null, null)).thenReturn(parsedDates);
            contextBuilderMock.when(() -> SummaryContextBuilder.build(parsedDates[0], parsedDates[1], ReportStatus.COMPLETED,
                            "all", reports, null, "Data malejąco"))
                    .thenReturn(expectedContext);

            //when
            Context result = summaryDataService.prepareSummaryContext(request);

            //then
            assertNotNull(result);
            assertNull(result.getVariable("showReportsTable"));
            assertNull(result.getVariable("showCharts"));
            
            verifyNoInteractions(emailUtils);
            verifyNoInteractions(chartDataPreparer);
        }
    }

    @Test
    void shouldPrepareContextWithOnlyTableData() {
        //given
        SummaryFormRequest request = mock(SummaryFormRequest.class);
        when(request.showReportsTable()).thenReturn(true);
        when(request.showCharts()).thenReturn(false);
        when(request.user()).thenReturn("all");

        LocalDate[] parsedDates = {LocalDate.now().minusDays(1), LocalDate.now()};
        List<ReportDto> reports = Collections.emptyList();

        EmailUtils.EmailPartsMap emailParts = mock(EmailUtils.EmailPartsMap.class);
        when(emailUtils.extractEmailParts(reports)).thenReturn(emailParts);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<SummaryContextBuilder> contextBuilderMock = mockStatic(SummaryContextBuilder.class)) {

            dateUtilsMock.when(() -> DateUtils.checkAnyDateIsNull(any(), any())).thenReturn(parsedDates);
            contextBuilderMock.when(() -> SummaryContextBuilder.build(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(new Context());

            //when
            Context result = summaryDataService.prepareSummaryContext(request);

            //then
            assertTrue((Boolean) result.getVariable("showReportsTable"));
            assertNull(result.getVariable("showCharts"));
            verify(emailUtils, times(1)).extractEmailParts(reports);
            verifyNoInteractions(chartDataPreparer);
        }
    }

    @Test
    void shouldPrepareContextWithOnlyChartData() {
        //given
        SummaryFormRequest request = mock(SummaryFormRequest.class);
        when(request.showReportsTable()).thenReturn(false);
        when(request.showCharts()).thenReturn(true);
        when(request.user()).thenReturn("all");

        LocalDate[] parsedDates = {LocalDate.now().minusDays(1), LocalDate.now()};
        List<ReportDto> reports = Collections.emptyList();

        Map<String, Object> chartData = Map.of("categoryChart", "chartData");
        when(chartDataPreparer.prepareCharts(reports, reports, parsedDates[0], parsedDates[1], false)).thenReturn(chartData);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class);
             MockedStatic<SummaryContextBuilder> contextBuilderMock = mockStatic(SummaryContextBuilder.class)) {

            dateUtilsMock.when(() -> DateUtils.checkAnyDateIsNull(any(), any())).thenReturn(parsedDates);
            contextBuilderMock.when(() -> SummaryContextBuilder.build(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(new Context());

            //when
            Context result = summaryDataService.prepareSummaryContext(request);

            //then
            assertTrue((Boolean) result.getVariable("showCharts"));
            assertNull(result.getVariable("showReportsTable"));
            verify(chartDataPreparer, times(1)).prepareCharts(reports, reports, parsedDates[0], parsedDates[1], false);
            verifyNoInteractions(emailUtils);
        }
    }
}
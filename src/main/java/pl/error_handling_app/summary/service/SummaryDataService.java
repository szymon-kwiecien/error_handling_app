package pl.error_handling_app.summary.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
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
import java.util.List;
import java.util.Map;

@Service
public class SummaryDataService {

    private static final String ALL_USERS = "all";

    private final ReportService reportService;
    private final ReportFilterService reportFilterService;
    private final ChartDataPreparer chartDataPreparer;
    private final ReportCategoryService reportCategoryService;
    private final EmailUtils emailUtils;

    public SummaryDataService(ReportService reportService, ReportFilterService reportFilterService,
                              ChartDataPreparer chartDataPreparer, ReportCategoryService reportCategoryService, EmailUtils emailUtils) {
        this.reportService = reportService;
        this.reportFilterService = reportFilterService;
        this.chartDataPreparer = chartDataPreparer;
        this.reportCategoryService = reportCategoryService;
        this.emailUtils = emailUtils;
    }

    public Context prepareSummaryContext(SummaryFormRequest formRequest) {
        var dateRange = resolveDateRange(formRequest);

        ReportCategory category = reportCategoryService.getCategoryByName(formRequest.categoryName())
                .orElse(null);

        List<ReportDto> reports = reportFilterService.prepareFilteredReports(
                formRequest, category, dateRange.from(), dateRange.to());

        String sortedBy = reportService.sortReports(reports, formRequest.sort());

        Context context = SummaryContextBuilder.build(
                dateRange.from(), dateRange.to(), formRequest.status(), formRequest.user(), reports, category, sortedBy);

        if (formRequest.showReportsTable()) {
            enrichContextWithTableData(context, reports);
        }

        if (formRequest.showCharts()) {
            enrichContextWithChartData(context, reports, formRequest, dateRange);
        }

        return context;
    }

    private DateRange resolveDateRange(SummaryFormRequest request) {
        LocalDate[] dates = DateUtils.checkAnyDateIsNull(request.dateFrom(), request.dateTo());
        return new DateRange(dates[0], dates[1]);
    }

    private void enrichContextWithTableData(Context context, List<ReportDto> reports) {
        EmailUtils.EmailPartsMap emailParts = emailUtils.extractEmailParts(reports);
        context.setVariable("remainingTime", reportService.calculateRemainingTime(reports));
        context.setVariable("reportingUserEmailLocalPart", emailParts.reportingLocalPart());
        context.setVariable("reportingUserEmailDomain", emailParts.reportingDomain());
        context.setVariable("assignedUserEmailLocalPart", emailParts.assignedLocalPart());
        context.setVariable("assignedUserEmailDomain", emailParts.assignedDomain());
        context.setVariable("showReportsTable", true);
    }

    private void enrichContextWithChartData(Context context, List<ReportDto> reports,
                                            SummaryFormRequest request, DateRange dates) {
        context.setVariable("showCharts", true);
        boolean isSingleEmployee = !ALL_USERS.equals(request.user());

        Map<String, Object> chartData = chartDataPreparer.prepareCharts(
                reports,
                reports,
                dates.from(),
                dates.to(),
                isSingleEmployee
        );

        context.setVariable("categoryReportChart", chartData.get("categoryChart"));
        context.setVariable("statusReportChart", chartData.get("statusChart"));
        context.setVariable("averageCompletionTimeChart", chartData.get("averageCompletionTimeChart"));
    }

    private record DateRange(LocalDate from, LocalDate to) {}
}
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SummaryDataService {

    private final ReportService reportService;
    private final ReportFilterService reportFilterService;
    private final ChartDataPreparer chartDataPreparer;
    private final ReportCategoryService reportCategoryService;
    private final EmailUtils emailUtils;

    public SummaryDataService(ReportService reportService,
                              ReportFilterService reportFilterService,
                              ChartDataPreparer chartDataPreparer,
                              ReportCategoryService reportCategoryService,
                              EmailUtils emailUtils) {
        this.reportService = reportService;
        this.reportFilterService = reportFilterService;
        this.chartDataPreparer = chartDataPreparer;
        this.reportCategoryService = reportCategoryService;
        this.emailUtils = emailUtils;
    }

    public Context prepareSummaryContext(SummaryFormRequest formRequest) {
        LocalDate[] localDates = DateUtils.checkAnyDateIsNull(formRequest.getDateFrom(), formRequest.getDateTo());
        formRequest.setDateFrom(localDates[0]);
        formRequest.setDateTo(localDates[1]);

        ReportCategory category = reportCategoryService.getCategoryByName(formRequest.getCategoryName()).orElse(null);
        List<ReportDto> reports = reportFilterService.prepareFilteredReports(formRequest, category);
        List<ReportDto> allFoundReports = new ArrayList<>(reports);

        String sortedBy = reportService.sortReports(reports, formRequest.getSort());

        Context context = SummaryContextBuilder.build(
                formRequest.getDateFrom(),
                formRequest.getDateTo(),
                formRequest.getStatus(),
                formRequest.getUser(),
                reports,
                category,
                sortedBy
        );

        if (formRequest.isShowReportsTable()) {
            enrichContextWithTableData(context, reports, formRequest.isShowReportsTable());
        }

        if (formRequest.isShowCharts()) {
            enrichContextWithChartData(context, reports, allFoundReports, formRequest);
        }

        return context;
    }

    private void enrichContextWithTableData(Context context, List<ReportDto> reports, boolean showTable) {
        EmailUtils.EmailPartsMap emailParts = emailUtils.extractEmailParts(reports);
        context.setVariable("remainingTime", reportService.calculateRemainingTime(reports));
        context.setVariable("reportingUserEmailLocalPart", emailParts.reportingLocalPart());
        context.setVariable("reportingUserEmailDomain", emailParts.reportingDomain());
        context.setVariable("assignedUserEmailLocalPart", emailParts.assignedLocalPart());
        context.setVariable("assignedUserEmailDomain", emailParts.assignedDomain());
        context.setVariable("showReportsTable", showTable);
    }

    private void enrichContextWithChartData(Context context, List<ReportDto> reports, List<ReportDto> allReports, SummaryFormRequest request) {
        context.setVariable("showCharts", true);
        boolean singleEmployee = !request.getUser().equals("all");

        Map<String, Object> chartData = chartDataPreparer.prepareCharts(
                reports,
                allReports,
                request.getDateFrom(),
                request.getDateTo(),
                singleEmployee
        );

        context.setVariable("categoryReportChart", chartData.get("categoryChart"));
        context.setVariable("statusReportChart", chartData.get("statusChart"));
        context.setVariable("averageCompletionTimeChart", chartData.get("averageCompletionTimeChart"));
    }
}
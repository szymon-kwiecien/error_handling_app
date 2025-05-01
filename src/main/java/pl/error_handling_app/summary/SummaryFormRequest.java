package pl.error_handling_app.summary;

import pl.error_handling_app.report.ReportStatus;

import java.time.LocalDate;

public class SummaryFormRequest {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String categoryName;
    private ReportStatus status;
    private String user = "all";
    private String sort;
    private boolean showReportsTable;
    private boolean showCharts;

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public boolean isShowReportsTable() {
        return showReportsTable;
    }

    public void setShowReportsTable(boolean showReportsTable) {
        this.showReportsTable = showReportsTable;
    }

    public boolean isShowCharts() {
        return showCharts;
    }

    public void setShowCharts(boolean showCharts) {
        this.showCharts = showCharts;
    }
}

package pl.error_handling_app.report;

public enum ReportStatus {

    PENDING("Oczekujące"),
    UNDER_REVIEW("W trakcie"),
    COMPLETED("Zakończone");

    public final String description;

    ReportStatus(String description) {
        this.description = description;
    }
}

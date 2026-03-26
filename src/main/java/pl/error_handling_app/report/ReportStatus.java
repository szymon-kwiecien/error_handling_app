package pl.error_handling_app.report;

public enum ReportStatus {

    PENDING("Oczekujące"),
    UNDER_REVIEW("W trakcie"),
    COMPLETED("Zakończone"),
    OVERDUE("Nieobsłużone w terminie");

    public final String description;

    public static ReportStatus getStatusFromString(String status) {
        for (ReportStatus rs : ReportStatus.values()) {
            if (rs.name().equalsIgnoreCase(status)) {
                return rs;
            }
        }
        return null;
    }

    ReportStatus(String description) {
        this.description = description;
    }
}

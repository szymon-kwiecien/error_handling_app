package pl.error_handling_app.summary;

import org.springframework.security.core.context.SecurityContextHolder;
import org.thymeleaf.context.Context;
import pl.error_handling_app.report.ReportCategory;
import pl.error_handling_app.report.ReportStatus;
import pl.error_handling_app.report.dto.ReportDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SummaryContextBuilder {

    public static Context build(LocalDate from, LocalDate to, ReportStatus status, String user,
                                List<ReportDto> reports, ReportCategory category, String sortedBy) {
        Context context = new Context();
        String summaryHeader = user.equals("all") ? "Raport dotyczący zgłoszeń" :
                "Raport dotyczący pracownika %s".formatted(user);
        context.setVariable("summaryHeader", summaryHeader);
        context.setVariable("reports", reports);
        context.setVariable("formattedDates", getFormattedDates(reports));
        context.setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        context.setVariable("currentUserName", SecurityContextHolder.getContext().getAuthentication().getName());
        context.setVariable("dateRange", from + " - " + to);
        context.setVariable("categories", category != null ? category.getName() : "Wszystkie");
        context.setVariable("status", status != null ? status.description : "Wszystkie");
        context.setVariable("sort", sortedBy);
        return context;
    }

    private static Map<Long, String> getFormattedDates(List<ReportDto> reports) {
        return reports.stream()
                .collect(Collectors.toMap(
                        ReportDto::getId,
                        r -> r.getDateAdded().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                ));
    }
}
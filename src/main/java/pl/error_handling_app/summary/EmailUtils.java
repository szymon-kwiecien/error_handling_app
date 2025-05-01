package pl.error_handling_app.summary;

import org.springframework.stereotype.Service;
import pl.error_handling_app.report.dto.ReportDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailUtils {

    public EmailPartsMap extractEmailParts(List<ReportDto> reports) {
        Map<Long, String> reportingLocalPart = new HashMap<>();
        Map<Long, String> reportingDomain = new HashMap<>();
        Map<Long, String> assignedLocalPart = new HashMap<>();
        Map<Long, String> assignedDomain = new HashMap<>();

        for (ReportDto report : reports) {
            if (report.getReportingUser() != null) {
                Map<String, String> parts = splitEmail(report.getReportingUser());
                reportingLocalPart.put(report.getId(), parts.get("localPart"));
                reportingDomain.put(report.getId(), parts.get("domain"));
            }

            if (report.getAssignedEmployee() != null) {
                Map<String, String> parts = splitEmail(report.getAssignedEmployee());
                assignedLocalPart.put(report.getId(), parts.get("localPart"));
                assignedDomain.put(report.getId(), parts.get("domain"));
            }
        }

        return new EmailPartsMap(reportingLocalPart, reportingDomain, assignedLocalPart, assignedDomain);
    }

    public static Map<String, String> splitEmail(String email) {
        Map<String, String> emailParts = new HashMap<>();
        if (email != null && email.contains("@")) {
            String[] parts = email.split("@");
            emailParts.put("localPart", parts[0]);
            emailParts.put("domain", "@" + parts[1]);
        } else {
            emailParts.put("localPart", email != null ? email : "");
            emailParts.put("domain", "");
        }
        return emailParts;
    }

    public record EmailPartsMap(
            Map<Long, String> reportingLocalPart,
            Map<Long, String> reportingDomain,
            Map<Long, String> assignedLocalPart,
            Map<Long, String> assignedDomain
    ) {}
}













package pl.error_handling_app.chat;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import pl.error_handling_app.report.Report;
import pl.error_handling_app.report.ReportRepository;
import pl.error_handling_app.user.User;
import pl.error_handling_app.user.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ChatController(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/report")
    public String showChatPage(@RequestParam("id") Long reportId, Authentication authentication, Model model) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow();
        Set<String> currentUserRoles = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        if(hasAccessToChat(user, report)) {
            model.addAttribute("report", report);
            model.addAttribute("reportId", reportId);
            model.addAttribute("attachments", report.getAttachments());
            model.addAttribute("username", username);

            switch (report.getStatus()){
                case COMPLETED -> model.addAttribute("statusColor", "green");
                case PENDING -> model.addAttribute("statusColor", "red");
                case UNDER_REVIEW -> model.addAttribute("statusColor", "yellow");
            }

            int timeToRespondProgress = calculateProgress(report.getDatedAdded(), report.getTimeToRespond());
            int timeToResolveProgress = calculateProgress(report.getDatedAdded(), report.getDueDate());
            model.addAttribute("timeToRespondProgress", timeToRespondProgress);
            model.addAttribute("timeToResolveProgress", timeToResolveProgress);
            model.addAttribute("timeToRespondColor", getProgressColor(timeToRespondProgress));
            model.addAttribute("timeToResolveColor", getProgressColor(timeToResolveProgress));

            if (currentUserRoles.contains("ROLE_ADMINISTRATOR")) {
                model.addAttribute("employees", userRepository.findALlByRoles_Name("EMPLOYEE"));
            }
            return "chat-report-details";
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);

    }


    private boolean hasAccessToChat(User user, Report report) {
        if(user.hasRole("ADMINISTRATOR") || report.getReportingUser().getEmail().contains(user.getEmail())) {
            return true;
        }
        return report.getAssignedEmployee() != null && report.getAssignedEmployee().getEmail().contains(user.getEmail());
    }

    private int calculateProgress(LocalDateTime startTime, LocalDateTime endTime) {

        LocalDateTime now = LocalDateTime.now();
        long totalDuration = Duration.between(startTime, endTime).toSeconds();
        long elapseDuration = Duration.between(startTime, now).toSeconds();

        if (elapseDuration <= 0){
            return 1;
        }
        if (elapseDuration >= totalDuration){
            return 100;
        }

        double progress = ((double) elapseDuration / totalDuration) * 100;
        return (int) Math.round(progress);
    }

    private String getProgressColor(int progress) {
        if (progress >= 75) {
            return "red";
        } else if (progress >= 50) {
            return "yellow";
        } else {
            return "green";
        }
    }

}

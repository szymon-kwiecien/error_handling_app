package pl.error_handling_app.chat;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.error_handling_app.report.dto.ReportDetailsDto;
import pl.error_handling_app.report.ReportService;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.user.dto.UserDetailsDto;
import pl.error_handling_app.user.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private final UserService userService;
    private final ReportService reportService;
    private final ChatService chatService;

    public ChatController(UserService userService, ReportService reportService, ChatService chatService) {
        this.userService = userService;
        this.reportService = reportService;
        this.chatService = chatService;
    }

    @GetMapping("/report")
    public String showChatPage(@RequestParam("id") Long reportId, Authentication authentication, Model model) {
        ReportDetailsDto report = reportService.findReportById(reportId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String username = SecurityContextHolder.getContext().getAuthentication().getName(); //w mojej aplikacji jest to adres e-mail
        UserDetailsDto user = userService.findUserDetailsByEmail(username).orElseThrow();
        Set<String> currentUserRoles = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        if (hasAccessToChat(user, report)) {
            model.addAttribute("report", report);
            model.addAttribute("reportId", reportId);
            model.addAttribute("attachments", report.getAttachments());
            model.addAttribute("username", username);

            switch (report.getStatus()) {
                case PENDING -> {
                    model.addAttribute("statusColor", "orange");
                    model.addAttribute("remainingTimeToFirstRespond", getRemainingTime(report, true));
                    model.addAttribute("remainingTimeToComplete", getRemainingTime(report, false));
                }
                case UNDER_REVIEW -> {
                    model.addAttribute("statusColor", "yellow");
                    model.addAttribute("remainingTimeToComplete", getRemainingTime(report, false));
                }
                case COMPLETED -> model.addAttribute("statusColor", "green");
                case OVERDUE -> model.addAttribute("statusColor", "red");
            }

            int timeToRespondProgress = calculateLeftTimePercentage(report, true);
            int timeToResolveProgress = calculateLeftTimePercentage(report, false);
            model.addAttribute("timeToRespondProgress", timeToRespondProgress);
            model.addAttribute("timeToResolveProgress", timeToResolveProgress);
            model.addAttribute("timeToRespondColor", getProgressColor(timeToRespondProgress));
            model.addAttribute("timeToResolveColor", getProgressColor(timeToResolveProgress));

            if (currentUserRoles.contains("ROLE_ADMINISTRATOR")) {
                model.addAttribute("employees", userService.findUsersByRoleName("EMPLOYEE"));
            }
            return "chat-report-details";
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }


    @PostMapping("api/chat/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageDto dto) {
        try {
            chatService.sendMessage(dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/api/chat/history/{reportId}")
    @ResponseBody
    public Page<ChatMessageDto> getMessages(@PathVariable Long reportId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return chatService.getMessagesForReport(reportId, page, size);
    }


    private boolean hasAccessToChat(UserDetailsDto user, ReportDetailsDto report) {
        if (user.getRoles().contains("ADMINISTRATOR") || report.getReportingUser().equals(user.getEmail())) {
            return true;
        }
        return report.getAssignedEmployee() != null && report.getAssignedEmployee().equals(user.getEmail());
    }

    private String getProgressColor(int progress) {
        if (progress >= 70) {
            return "green";
        } else if (progress >= 25) {
            return "yellow";
        } else {
            return "red";
        }
    }

    private int calculateLeftTimePercentage(ReportDetailsDto report, boolean forFirstRespond) {
        return reportService.calculateTimeLeftPercentage(buildReportDto(report, forFirstRespond));
    }

    private String getRemainingTime(ReportDetailsDto report, boolean forFirstRespond) {
        return reportService.calculateRemainingTime(List.of(buildReportDto(report, forFirstRespond)))[0];
    }

    private ReportDto buildReportDto(ReportDetailsDto report, boolean forFirstRespond) {
        ReportDto reportDto = new ReportDto();
        reportDto.setDateAdded(report.getDateAdded());
        reportDto.setToRespondDate(report.getTimeToRespond());
        reportDto.setDueDate(report.getDueDate());
        reportDto.setStatusName(forFirstRespond
                ? report.getStatus().description
                : "W trakcie"); //ustawiam taki status aby dla zgłoszeń oczekujących również móc wyświetlić
        //czas pozostały na rozwiązanie (poza pozostałym czasem na pierwszą reakcję) oraz obliczyć procentowo pozostałą ilość czasu na rozwiązanie
        // (poza procentem pozostałego czasu na pierwszą reakcję)
        return reportDto;
    }

}

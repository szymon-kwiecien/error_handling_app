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
import pl.error_handling_app.report.ReportStatus;
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
        ReportDetailsDto report = reportService.getReportForChat(reportId, authentication.getName());
        model.addAttribute("report", report);
        model.addAttribute("reportId", reportId);
        model.addAttribute("attachments", report.getAttachments());
        model.addAttribute("username", authentication.getName());
        prepareChatModel(model, report, authentication);

        return "chat-report-details";
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
                                            @RequestParam(defaultValue = "20") int size,
                                            Authentication authentication) {

        reportService.getReportForChat(reportId, authentication.getName());
        return chatService.getMessagesForReport(reportId, page, size);
    }


    private void prepareChatModel(Model model, ReportDetailsDto report, Authentication authentication) {
        model.addAttribute("statusColor", getStatusColor(report.getStatus()));

        if (report.getStatus() == ReportStatus.PENDING) {
            model.addAttribute("remainingTimeToFirstRespond", getRemainingTime(report, true));
            model.addAttribute("remainingTimeToComplete", getRemainingTime(report, false));
        } else if (report.getStatus() == ReportStatus.UNDER_REVIEW) {
            model.addAttribute("remainingTimeToComplete", getRemainingTime(report, false));
        }

        int respondProgress = calculateLeftTimePercentage(report, true);
        int resolveProgress = calculateLeftTimePercentage(report, false);

        model.addAttribute("timeToRespondProgress", respondProgress);
        model.addAttribute("timeToResolveProgress", resolveProgress);
        model.addAttribute("timeToRespondColor", getProgressColor(respondProgress));
        model.addAttribute("timeToResolveColor", getProgressColor(resolveProgress));

        if (hasRole(authentication, "ROLE_ADMINISTRATOR")) {
            model.addAttribute("employees", userService.findUsersByRoleName("EMPLOYEE"));
        }
    }

    private String getStatusColor(ReportStatus status) {
        return switch (status) {
            case PENDING -> "orange";
            case UNDER_REVIEW -> "yellow";
            case COMPLETED -> "green";
            case OVERDUE -> "red";
        };
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

    private boolean hasRole(Authentication authentication, String roleName) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(roleName::equals);
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

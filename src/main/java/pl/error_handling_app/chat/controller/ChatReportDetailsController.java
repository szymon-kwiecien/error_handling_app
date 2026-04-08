package pl.error_handling_app.chat.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.error_handling_app.chat.dto.ChatMessageDto;
import pl.error_handling_app.chat.helper.ReportDetailsViewHelper;
import pl.error_handling_app.chat.service.ChatService;
import pl.error_handling_app.report.dto.ReportDetailsDto;
import pl.error_handling_app.report.service.ReportService;
import pl.error_handling_app.user.service.UserService;
import pl.error_handling_app.utils.SecurityUtils;

@Controller
public class ChatReportDetailsController {

    private final UserService userService;
    private final ReportService reportService;
    private final ChatService chatService;
    private final ReportDetailsViewHelper viewHelper;

    public ChatReportDetailsController(UserService userService, ReportService reportService, ChatService chatService, ReportDetailsViewHelper viewHelper) {
        this.userService = userService;
        this.reportService = reportService;
        this.chatService = chatService;
        this.viewHelper = viewHelper;
    }

    @GetMapping("/report")
    public String showChatPage(@RequestParam("id") Long reportId, Authentication authentication, Model model) {
        ReportDetailsDto report = reportService.getReportForChat(reportId, authentication.getName());
        model.addAttribute("report", report);
        model.addAttribute("reportId", reportId);
        model.addAttribute("attachments", report.getAttachments());
        model.addAttribute("username", authentication.getName());
        viewHelper.prepareReportViewModel(model, report);
        if (SecurityUtils.isAdmin(authentication)) {
            model.addAttribute("employees", userService.findUsersByRoleName("EMPLOYEE"));
        }
        return "chat/chat-report-details";
    }


    @PostMapping("/api/chat/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageDto dto, Authentication authentication) {
        try {
            chatService.sendMessage(dto, authentication.getName());
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

}

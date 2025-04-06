package pl.error_handling_app;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.error_handling_app.report.*;

import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportCategoryService reportCategoryService;

    public ReportController(ReportService reportService, ReportCategoryService reportCategoryService) {
        this.reportService = reportService;
        this.reportCategoryService = reportCategoryService;
    }


    @GetMapping()
    public String listReports(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size,
                              @RequestParam(defaultValue = "all") String status,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "addedDateDesc")String sort,Model model) {

        Sort sorting = getSort(sort);
        Pageable pageable = PageRequest.of(--page, size, sorting);
        ReportStatus reportStatus = status.equals("all") ? null : ReportStatus.valueOf(status);
        Page<ReportDto> reports = reportService.findReports(search, reportStatus, pageable);
        reports.forEach(report -> report.setLeftTimePercentage(reportService.calculateTimeLeftPercentage(report)));
        model.addAttribute("reports", reports);
        model.addAttribute("currentPage", ++page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", reports.getTotalPages());
        model.addAttribute("status", status);
        model.addAttribute("search",search);
        model.addAttribute("sort", sort);
        return "report-listing";
    }

    @GetMapping("/add")
    public String addReportForm(Model model) {
        model.addAttribute("report", new NewReportDto());
        model.addAttribute("categories", reportCategoryService.getAllCategories());
        return "add-new-report";
    }

    @PostMapping(value = "/add", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String addReport(NewReportDto report, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            return "add-new-report";        // wkrótce dodam walidację
        }
        try {
            reportService.addNewReport(report);
        } catch(Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Wystąpił błąd podczas dodawania zgłoszenia. Spróbuj ponownie.";
            model.addAttribute("error", errorMessage);
            model.addAttribute("report",report);
            model.addAttribute("categories", reportCategoryService.getAllCategories());
            return "add-new-report";
        }
            return "redirect:/reports/add?success";
    }

    @Secured("ROLE_ADMINISTRATOR")
    @PostMapping("/delete")
    public String deleteReport(@RequestParam Long reportId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String currentUserName = authentication.getName();
        try {
            reportService.deleteReport(reportId, currentUserName);
            redirectAttributes.addFlashAttribute("successMessage", "Zgłoszenie zostało pomyślnie usunięte");
            return "redirect:/reports";
        } catch(ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Podczas usuwania zgłoszenia wystąpił błąd: " + e.getReason());
            return "redirect:/reports";
        }
    }

    @Secured({"ROLE_ADMINISTRATOR", "ROLE_EMPLOYEE"})
    @PostMapping("/close")
    public String closeReport(@RequestParam Long reportId, Authentication authentication, RedirectAttributes redirectAttributes) {
        String currentUserName = authentication.getName();
        try {
            reportService.closeReport(reportId, currentUserName);
            redirectAttributes.addFlashAttribute("successMessage", "Zgłoszenie zostało zamknięte.");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Podczas zamykania zgłoszenia wystąpił błąd: " + e.getReason());
        }
        return "redirect:/report?id=" + reportId;
    }

    @Secured("ROLE_ADMINISTRATOR")
    @PostMapping("/assign")
    public String assignEmployeeToReport(@RequestParam Long reportId, @RequestParam Long employeeId,
                                         RedirectAttributes redirectAttributes) {

        try {
            reportService.assignEmployeeToReport(reportId, employeeId);
            redirectAttributes.addFlashAttribute("successMessage", "Pracownik został przypisany do zgłoszenia.");
        } catch(IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Podczas przypisywania pracownika wystąpił błąd: " + e.getMessage());
        }
        return "redirect:/report?id=" + reportId;
    }

    @PostMapping("/attachment/upload")
    public String uploadAttachment(@RequestParam Long reportId, @RequestParam List<MultipartFile> files,
                                   RedirectAttributes redirectAttributes) {
        try {
            reportService.addAttachmentsToExistingReport(reportId, files);
            redirectAttributes.addFlashAttribute("successMessage", "Załączniki zostały dodane pomyślnie.");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono zgłoszenia.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas dodawania załączników: " + e.getMessage());
        }
        return "redirect:/report?id=" + reportId;
    }


    private Sort getSort(String sort) {
        return switch (sort) {
            case "addedDateAsc" -> Sort.by("datedAdded").ascending();
            case "remainingTimeAsc" -> Sort.by("dueDate").ascending();
            case "remainingTimeDesc" -> Sort.by("dueDate").descending();
            default -> Sort.by("datedAdded").descending(); //domyslnie sortuje wg daty dodania (od najnowszych)
        };

}
}

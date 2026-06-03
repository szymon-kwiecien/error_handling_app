package pl.error_handling_app.report.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.error_handling_app.report.dto.NewReportDto;
import pl.error_handling_app.report.dto.ReportDto;
import pl.error_handling_app.report.service.ReportCategoryService;
import pl.error_handling_app.report.service.ReportService;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(ReportControllerTest.MethodSecurityConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private ReportCategoryService reportCategoryService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @MockitoBean
    private H2ConsoleProperties h2ConsoleProperties;

    @TestConfiguration
    @EnableMethodSecurity(securedEnabled = true)
    static class MethodSecurityConfig {
    }

    @Test
    @WithMockUser
    void shouldReturnReportListingPage() throws Exception {
        //given
        Page<ReportDto> mockPage = new PageImpl<>(List.of(mock(ReportDto.class)));
        when(reportService.findReports(any(), any(), any())).thenReturn(mockPage);
        when(reportService.calculateRemainingTime(anyList())).thenReturn(new String[]{"2 dni"});

        //when, then
        mockMvc.perform(get("/reports")
                        .param("page", "1")
                        .param("size", "5")
                        .param("status", "all")
                        .param("search", "")
                        .param("sort", "addedDateDesc"))
                .andExpect(status().isOk())
                .andExpect(view().name("report/report-listing"))
                .andExpect(model().attributeExists("reports", "reportsRemainingTimes", "currentPage", "totalPages"));
    }

    @Test
    @WithMockUser
    void shouldReturnAddReportForm() throws Exception {
        //given
        when(reportCategoryService.getAllCategories()).thenReturn(List.of());

        //when, then
        mockMvc.perform(get("/reports/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("report/add-new-report"))
                .andExpect(model().attributeExists("report", "categories"));
    }

    @Test
    @WithMockUser
    void shouldSubmitNewReportAndRedirectOnSuccess() throws Exception {
        //given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Test content".getBytes()
        );

        //when, then
        mockMvc.perform(multipart("/reports/add")
                        .file(mockFile)
                        .param("title", "Tytuł zgłoszenia ktory jest poprawny")
                        .param("description", "Opis zgloszenia ktory jest poprawny")
                        .param("categoryId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports/add?success"));

        verify(reportService, times(1)).addNewReport(any(NewReportDto.class));
    }

    @Test
    @WithMockUser
    void shouldReturnFormWithErrorsWhenNewReportIsInvalid() throws Exception {
        //when, then
        mockMvc.perform(multipart("/reports/add")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("report/add-new-report"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));

        verify(reportService, never()).addNewReport(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMINISTRATOR")
    void shouldAllowAdminToDeleteReport() throws Exception {
        //when, then
        mockMvc.perform(post("/reports/delete")
                        .param("reportId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(reportService, times(1)).deleteReport(eq(1L), anyString());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldDenyUserToDeleteReport() throws Exception {
        //when, then
        mockMvc.perform(post("/reports/delete")
                        .param("reportId", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(reportService, never()).deleteReport(anyLong(), anyString());
    }

    @Test
    @WithMockUser(authorities = "ROLE_EMPLOYEE")
    void shouldAllowEmployeeToCloseReport() throws Exception {
        //when, then
        mockMvc.perform(post("/reports/close")
                        .param("reportId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/report?id=1"));

        verify(reportService, times(1)).closeReport(eq(1L), anyString());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldDenyUserToCloseReport() throws Exception {
        //when, then
        mockMvc.perform(post("/reports/close")
                        .param("reportId", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMINISTRATOR")
    void shouldAllowAdminToAssignEmployee() throws Exception {
        //when, then
        mockMvc.perform(post("/reports/assign")
                        .param("reportId", "1")
                        .param("employeeId", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/report?id=1"));

        verify(reportService, times(1)).assignEmployeeToReport(1L, 2L);
    }

    @Test
    @WithMockUser
    void shouldUploadAttachmentSuccessfully() throws Exception {
        //given
        MockMultipartFile mockFile = new MockMultipartFile(
                "files", "test.pdf", "application/pdf", "Content".getBytes()
        );

        //when, then
        mockMvc.perform(multipart("/reports/attachment/upload")
                        .file(mockFile)
                        .param("reportId", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/report?id=1"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(reportService, times(1)).addAttachmentsToExistingReport(eq(1L), anyList());
    }
}
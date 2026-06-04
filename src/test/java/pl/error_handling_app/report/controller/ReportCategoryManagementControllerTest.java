package pl.error_handling_app.report.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.service.ReportCategoryService;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportCategoryManagementController.class)
@Import(ReportCategoryManagementControllerTest.MethodSecurityConfig.class)
class ReportCategoryManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldReturnManageCategoriesPage() throws Exception {
        //given
        Page<ReportCategoryDto> mockPage = new PageImpl<>(List.of(new ReportCategoryDto(1L, "Kategoria 1")));
        when(reportCategoryService.getPagedCategories(any(Pageable.class))).thenReturn(mockPage);

        //when, then
        mockMvc.perform(get("/admin/manage-categories")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("report/manage-categories"))
                .andExpect(model().attributeExists("categories", "currentPage", "pageSize", "totalPages", "newCategory"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldAddCategoryAndRedirectOnSuccess() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/add-category")
                        .param("name", "Nowa kategoria")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-categories?page=1&size=10"))
                .andExpect(flash().attributeExists("success"));

        verify(reportCategoryService, times(1)).addCategory(any(ReportCategoryDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldReturnFormWhenAddCategoryIsInvalid() throws Exception {
        //given
        Page<ReportCategoryDto> mockPage = new PageImpl<>(List.of());
        when(reportCategoryService.getPagedCategories(any(Pageable.class))).thenReturn(mockPage);

        //when, then
        mockMvc.perform(post("/admin/add-category")
                        .param("name", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("report/manage-categories"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("categories"));

        verify(reportCategoryService, never()).addCategory(any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldEditCategoryAndRedirectOnSuccess() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/edit-category/1")
                        .param("name", "Zmieniona Kategoria")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-categories?page=1&size=10"))
                .andExpect(flash().attributeExists("success"));

        verify(reportCategoryService, times(1)).editCategory(eq(1L), any(ReportCategoryDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldRedirectWithFlashErrorsWhenEditCategoryIsInvalid() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/edit-category/1")
                        .param("name", "")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-categories?page=1&size=10"))
                .andExpect(flash().attributeExists("editErrors"));

        verify(reportCategoryService, never()).editCategory(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void shouldDeleteCategoryAndRedirect() throws Exception {
        //when, then
        mockMvc.perform(post("/admin/delete-category/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-categories?page=1&size=10"))
                .andExpect(flash().attributeExists("success"));

        verify(reportCategoryService, times(1)).deleteCategory(1L);
    }
}
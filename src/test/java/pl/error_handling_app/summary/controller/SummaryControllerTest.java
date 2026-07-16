package pl.error_handling_app.summary.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.context.Context;
import pl.error_handling_app.report.dto.ReportCategoryDto;
import pl.error_handling_app.report.service.ReportCategoryService;
import pl.error_handling_app.summary.dto.SummaryFormRequest;
import pl.error_handling_app.summary.service.SummaryDataService;
import pl.error_handling_app.summary.service.SummaryPdfGenerator;
import pl.error_handling_app.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SummaryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportCategoryService reportCategoryService;

    @Mock
    private SummaryDataService summaryDataService;

    @Mock
    private SummaryPdfGenerator pdfGenerator;

    @InjectMocks
    private SummaryController summaryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(summaryController).build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnSummariesPanelWithEmployeesForAdmin() throws Exception {
        //given
        setupSecurityContext("admin@test.pl", "ROLE_ADMINISTRATOR");

        ReportCategoryDto categoryMock = mock(ReportCategoryDto.class);
        when(categoryMock.name()).thenReturn("Sprzęt IT");
        when(reportCategoryService.getAllCategories()).thenReturn(List.of(categoryMock));

        List<Object> mockEmployees = List.of(new Object());
        when(userRepository.findALlByRoles_Name("EMPLOYEE")).thenReturn(Collections.emptyList());

        //when
        mockMvc.perform(get("/summaries"))

                //then
                .andExpect(status().isOk())
                .andExpect(view().name("summary/summaries-panel"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("statusList"))
                .andExpect(model().attribute("employees", Collections.emptyList()))
                .andExpect(model().attributeDoesNotExist("currentEmployee"));

        verify(reportCategoryService, times(1)).getAllCategories();
        verify(userRepository, times(1)).findALlByRoles_Name("EMPLOYEE");
    }

    @Test
    void shouldReturnSummariesPanelWithCurrentEmployeeForNonAdmin() throws Exception {
        //given
        String currentUserEmail = "pracownik@test.pl";
        setupSecurityContext(currentUserEmail, "ROLE_EMPLOYEE");

        ReportCategoryDto categoryMock = mock(ReportCategoryDto.class);
        when(categoryMock.name()).thenReturn("Oprogramowanie");
        when(reportCategoryService.getAllCategories()).thenReturn(List.of(categoryMock));

        //when
        mockMvc.perform(get("/summaries"))

                //then
                .andExpect(status().isOk())
                .andExpect(view().name("summary/summaries-panel"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("statusList"))
                .andExpect(model().attribute("currentEmployee", currentUserEmail))
                .andExpect(model().attributeDoesNotExist("employees"));

        verify(reportCategoryService, times(1)).getAllCategories();
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldGenerateSummaryPdfSuccessfully() throws Exception {
        //given
        Context mockContext = mock(Context.class);
        when(summaryDataService.prepareSummaryContext(any(SummaryFormRequest.class))).thenReturn(mockContext);

        //when
        mockMvc.perform(post("/generate-summary")
                        .param("categoryName", "Sprzęt IT")
                        .param("user", "pracownik@test.pl")
                        .param("status", "UNDER_REVIEW"))

                //then
                .andExpect(status().isOk());

        verify(summaryDataService, times(1)).prepareSummaryContext(any(SummaryFormRequest.class));
        verify(pdfGenerator, times(1)).generatePdf(
                eq("summary/summary-template"),
                eq(mockContext),
                any(HttpServletResponse.class)
        );
    }

    private void setupSecurityContext(String username, String roleName) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        doReturn(Collections.singletonList(authority)).when(authentication).getAuthorities();
        lenient().when(authentication.getName()).thenReturn(username);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
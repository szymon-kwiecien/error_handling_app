package pl.error_handling_app.company.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import pl.error_handling_app.company.dto.CompanyDto;
import pl.error_handling_app.company.service.CompanyService;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CompanyManagementController.class)
@AutoConfigureMockMvc(addFilters = false) //Ominiecie filtrów Spring Security dla celow testowych kontrolera
class CompanyManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    void shouldReturnManageCompaniesPageWithPagination() throws Exception {
        //given
        CompanyDto company = new CompanyDto(1L, "Firma Testowa", 2, 24);
        PageImpl<CompanyDto> companyPage = new PageImpl<>(List.of(company), PageRequest.of(0, 10), 1);

        given(companyService.findPagedCompanies(any(Pageable.class))).willReturn(companyPage);

        //when, then
        mockMvc.perform(get("/admin/manage-companies")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("company/manage-companies"))
                .andExpect(model().attributeExists("companies", "newCompany"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("pageSize", 10))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    void shouldAddCompanyAndRedirectWhenDataIsValid() throws Exception {
        //given

        //when, then
        mockMvc.perform(post("/admin/add-company")
                        .param("name", "Nowa Firma")
                        .param("timeToFirstRespond", "4")
                        .param("timeToResolve", "48")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-companies?page=1&size=10"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Firma Nowa Firma została utworzona."));

        //Sprawdzam czy kontroler przekazal wywołanie do serwisu
        then(companyService).should().saveCompany(any(CompanyDto.class));
    }

    @Test
    void shouldNotAddCompanyAndReturnToFormWhenValidationFails() throws Exception {
        //given
        given(companyService.findPagedCompanies(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        //when then
        //błędne dane: nazwa "A" - (za krótka), czas 0 - (za mały)
        mockMvc.perform(post("/admin/add-company")
                        .param("name", "A")
                        .param("timeToFirstRespond", "0")
                        .param("timeToResolve", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk()) // Brak przekierowania
                .andExpect(view().name("company/manage-companies"))
                .andExpect(model().attributeHasFieldErrors("newCompany", "name", "timeToFirstRespond", "timeToResolve"));

        // Sprawdzenie czy wadliwe dane nie trafiły do serwisu
        then(companyService).should(never()).saveCompany(any());
    }

    @Test
    void shouldEditCompanyAndRedirectWhenDataIsValid() throws Exception {
        //given
        Long companyId = 5L;

        //when, then
        mockMvc.perform(post("/admin/edit-company/{id}", companyId)
                        .param("name", "Zaktualizowana Firma")
                        .param("timeToFirstRespond", "2")
                        .param("timeToResolve", "24")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-companies?page=2&size=5"))
                .andExpect(flash().attributeExists("success"));

        then(companyService).should().updateCompany(any(CompanyDto.class), eq(companyId));
    }

    @Test
    void shouldNotEditCompanyAndRedirectWithErrorsWhenValidationFails() throws Exception {
        //given
        Long companyId = 5L;

        //when, then
        //Błedne dane dla edycji
        mockMvc.perform(post("/admin/edit-company/{id}", companyId)
                        .param("name", "") // pusta nazwa - blad
                        .param("timeToFirstRespond", "2")
                        .param("timeToResolve", "24")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-companies?page=1&size=10"))
                .andExpect(flash().attributeExists("editErrors"));

        then(companyService).should(never()).updateCompany(any(), any());
    }

    @Test
    void shouldDeleteCompanyAndRedirect() throws Exception {
        //given
        Long companyId = 10L;

        //when, then
        mockMvc.perform(post("/admin/delete-company/{id}", companyId)
                        .param("page", "3")
                        .param("size", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/manage-companies?page=3&size=20"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Firma została usunięta."));

        then(companyService).should().deleteCompany(companyId);
    }
}
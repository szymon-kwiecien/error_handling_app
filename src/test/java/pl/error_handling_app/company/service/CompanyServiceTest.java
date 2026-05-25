package pl.error_handling_app.company.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.error_handling_app.company.dto.CompanyDto;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.company.repository.CompanyRepository;
import pl.error_handling_app.exception.CompanyAlreadyExistsException;
import pl.error_handling_app.exception.CompanyNotFoundException;
import pl.error_handling_app.user.service.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompanyService companyService;

    @Captor
    private ArgumentCaptor<Company> companyCaptor;

    @Test
    void shouldReturnAllCompanies() {
        //given
        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Firma A");

        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Firma B");

        given(companyRepository.findAll()).willReturn(List.of(company1, company2));

        //when
        List<CompanyDto> result = companyService.findAllCompanies();

        //then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Firma A");
        assertThat(result.get(1).name()).isEqualTo("Firma B");
    }

    @Test
    void shouldSaveCompanyWhenNameIsUnique() {
        //given
        CompanyDto newCompanyDto = new CompanyDto(null, "Nowa Firma", 2, 24);
        given(companyRepository.findByName("Nowa Firma")).willReturn(Optional.empty());

        //when
        companyService.saveCompany(newCompanyDto);

        //then
        then(companyRepository).should().save(companyCaptor.capture());
        Company savedCompany = companyCaptor.getValue();

        assertThat(savedCompany.getName()).isEqualTo("Nowa Firma");
        assertThat(savedCompany.getTimeToFirstRespond()).isEqualTo(2);
        assertThat(savedCompany.getTimeToResolve()).isEqualTo(24);
    }

    @Test
    void shouldThrowExceptionWhenSavingCompanyWithExistingName() {
        //given
        CompanyDto newCompanyDto = new CompanyDto(null, "Istniejąca Firma", 2, 24);
        Company existingCompany = new Company();
        existingCompany.setId(10L);
        existingCompany.setName("Istniejąca Firma");

        given(companyRepository.findByName("Istniejąca Firma")).willReturn(Optional.of(existingCompany));

        //when , then
        assertThatThrownBy(() -> companyService.saveCompany(newCompanyDto))
                .isInstanceOf(CompanyAlreadyExistsException.class)
                .hasMessageContaining("Firma o nazwie Istniejąca Firma już istnieje");

        // Sprawdzam czy repo nie zapisało danych
        then(companyRepository).should(never()).save(any());
    }

    @Test
    void shouldUpdateCompanyWhenValidDataProvided() {
        //given
        Long companyId = 1L;
        CompanyDto updateDto = new CompanyDto(companyId, "Zmieniona Nazwa", 4, 48);

        Company existingCompany = new Company();
        existingCompany.setId(companyId);
        existingCompany.setName("Stara Nazwa");

        given(companyRepository.findByName("Zmieniona Nazwa")).willReturn(Optional.empty());
        given(companyRepository.findById(companyId)).willReturn(Optional.of(existingCompany));

        //when
        companyService.updateCompany(updateDto, companyId);

        //then
        //Sprawdzam czy oryginaly obiekt z repozytorium został zmodyfikowany
        assertThat(existingCompany.getName()).isEqualTo("Zmieniona Nazwa");
        assertThat(existingCompany.getTimeToFirstRespond()).isEqualTo(4);
        assertThat(existingCompany.getTimeToResolve()).isEqualTo(48);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingAndNewNameIsTakenByAnotherCompany() {
        //given
        Long companyIdToUpdate = 1L;
        CompanyDto updateDto = new CompanyDto(companyIdToUpdate, "Nazwa Konkurencji", 4, 48);

        Company anotherCompany = new Company();
        anotherCompany.setId(99L);
        anotherCompany.setName("Nazwa Konkurencji");

        given(companyRepository.findByName("Nazwa Konkurencji")).willReturn(Optional.of(anotherCompany));

        // when,then
        assertThatThrownBy(() -> companyService.updateCompany(updateDto, companyIdToUpdate))
                .isInstanceOf(CompanyAlreadyExistsException.class)
                .hasMessageContaining("już istnieje");
    }

    @Test
    void shouldDeleteCompanyAndPrepareUsersWhenCompanyExists() {
        //given
        Long companyId = 5L;
        given(companyRepository.existsById(companyId)).willReturn(true);

        //when
        companyService.deleteCompany(companyId);

        //then
        //sprawdzam czy wywołano zależny serwis oraz kasowanie z bazy
        then(userService).should().prepareUsersForCompanyDeletion(companyId);
        then(companyRepository).should().deleteById(companyId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingCompany() {
        //given
        Long companyId = 99L;
        given(companyRepository.existsById(companyId)).willReturn(false);

        //when then
        assertThatThrownBy(() -> companyService.deleteCompany(companyId))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Firma nie została znaleziona.");

        then(userService).should(never()).prepareUsersForCompanyDeletion(any());
        then(companyRepository).should(never()).deleteById(any());
    }

    @Test
    void shouldReturnPagedCompanies() {
        //given
        Pageable pageable = PageRequest.of(0, 10); // pierwsza strona max 10 wyników

        Company company = new Company();
        company.setId(1L);
        company.setName("Stronicowana Firma");

        Page<Company> companyPage = new PageImpl<>(List.of(company));

        given(companyRepository.findAll(pageable)).willReturn(companyPage);

        //when
        Page<CompanyDto> result = companyService.findPagedCompanies(pageable);

        //then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Stronicowana Firma");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingCompany() {
        //given
        Long nonExistingCompanyId = 99L;
        CompanyDto updateDto = new CompanyDto(nonExistingCompanyId, "Firma nieistniejaca", 4, 48);

        given(companyRepository.findByName("Firma nieistniejaca")).willReturn(Optional.empty());
        given(companyRepository.findById(nonExistingCompanyId)).willReturn(Optional.empty());

        //when then
        assertThatThrownBy(() -> companyService.updateCompany(updateDto, nonExistingCompanyId))
                .isInstanceOf(CompanyNotFoundException.class)
                .hasMessage("Firma nie została znaleziona.");
    }
}
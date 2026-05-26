package pl.error_handling_app.company.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.error_handling_app.company.entity.Company;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    void shouldFindCompanyByNameWhenItExistsInDatabase() {
        //given
        Company company = new Company();
        company.setName("Firma testowa");
        company.setTimeToFirstRespond(4);
        company.setTimeToResolve(48);
        companyRepository.save(company);

        //when
        Optional<Company> foundCompany = companyRepository.findByName("Firma testowa");

        //then
        assertThat(foundCompany).isPresent();
        assertThat(foundCompany.get().getName()).isEqualTo("Firma testowa");
        assertThat(foundCompany.get().getTimeToFirstRespond()).isEqualTo(4);
    }

    @Test
    void shouldReturnEmptyOptionalWhenCompanyDoesNotExist() {
        //given

        //when
        Optional<Company> foundCompany = companyRepository.findByName("Firma nieistniejaca");

        //then
        assertThat(foundCompany).isEmpty();
    }
}
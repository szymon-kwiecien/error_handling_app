package pl.error_handling_app.company.dto;

import org.junit.jupiter.api.Test;
import pl.error_handling_app.company.entity.Company;
import static org.assertj.core.api.Assertions.assertThat;

class CompanyDtoMapperTest {

    @Test
    void shouldMapEntityToDto() {
        //given
        Company company = new Company();
        company.setId(30L);
        company.setName("Testowa nazwa");
        company.setTimeToFirstRespond(5);
        company.setTimeToResolve(14);

        //when
        CompanyDto dto = CompanyDtoMapper.map(company);

        //then
        assertThat(dto.id()).isEqualTo(30L);
        assertThat(dto.name()).isEqualTo("Testowa nazwa");
        assertThat(dto.timeToFirstRespond()).isEqualTo(5);
        assertThat(dto.timeToResolve()).isEqualTo(14);
    }

    @Test
    void shouldMapDtoToEntity() {
        //given
        CompanyDto dto = new CompanyDto(50L, "Nazwa firmy", 5, 14);

        //when
        Company entity = CompanyDtoMapper.map(dto);

        //then
        assertThat(entity.getId())
                .as("Przy mapowaniu dto na encję nie powinno być mapowane po ID")
                .isNull();

        assertThat(entity.getName()).isEqualTo("Nazwa firmy");
        assertThat(entity.getTimeToFirstRespond()).isEqualTo(5);
        assertThat(entity.getTimeToResolve()).isEqualTo(14);
    }
}
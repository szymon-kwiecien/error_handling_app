package pl.error_handling_app.company;

public class CompanyDtoMapper {

    static CompanyDto map(Company company) {
        return new CompanyDto(company.getId(), company.getName());
    }
}

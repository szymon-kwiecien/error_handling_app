package pl.error_handling_app.company;

public class CompanyDtoMapper {

    static CompanyDto map(Company company) {
        return new CompanyDto(company.getId(), company.getName(),
                company.getTimeToFirstRespond(), company.getTimeToResolve());
    }

    static Company map (CompanyDto companyDto) {
        Company company = new Company();
        company.setName(companyDto.getName());
        company.setTimeToFirstRespond(companyDto.getTimeToFirstRespond());
        company.setTimeToResolve(companyDto.getTimeToResolve());
        return company;
    }
}

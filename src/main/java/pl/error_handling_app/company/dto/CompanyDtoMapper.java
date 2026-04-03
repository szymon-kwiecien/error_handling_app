package pl.error_handling_app.company.dto;

import pl.error_handling_app.company.entity.Company;

public class CompanyDtoMapper {

    public static CompanyDto map(Company company) {
        return new CompanyDto(company.getId(), company.getName(),
                company.getTimeToFirstRespond(), company.getTimeToResolve());
    }

    public static Company map (CompanyDto companyDto) {
        Company company = new Company();
        company.setName(companyDto.getName());
        company.setTimeToFirstRespond(companyDto.getTimeToFirstRespond());
        company.setTimeToResolve(companyDto.getTimeToResolve());
        return company;
    }
}

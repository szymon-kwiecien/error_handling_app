package pl.error_handling_app.company;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<CompanyDto> findALlCompanies() {
        return companyRepository.findAll().stream().map(CompanyDtoMapper::map).toList();
    }
}

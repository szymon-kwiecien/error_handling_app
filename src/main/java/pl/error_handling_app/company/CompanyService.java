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

    public void saveCompany(CompanyDto companyToSave) {
        validateCompany(companyToSave, null);
        Company company = CompanyDtoMapper.map(companyToSave);
        companyRepository.save(company);
    }

    private void validateCompany(CompanyDto companyToSave, Long companyToUpdateId) {
        if (companyToSave.getTimeToFirstRespond() < 1 || companyToSave.getTimeToFirstRespond() > 48) {
            throw new IllegalArgumentException("Czas na pierwszą reakcję musi wynosić pomiędzy 1 a 48 godzin.");
        }
        if (companyToSave.getTimeToResolve() < 1 || companyToSave.getTimeToResolve() > 168) {
            throw new IllegalArgumentException("Czas na rozwiązanie musi być wynosić 1 a 168 godzin.");
        }
        if (companyRepository.findByName(companyToSave.getName()).isPresent()) {
            Long existingCompanyId = companyRepository.findByName(companyToSave.getName()).get().getId();
            if (companyToUpdateId == null || !existingCompanyId.equals(companyToUpdateId)) {
                throw new IllegalArgumentException("Firma o podanej nazwie już istnieje: " + companyToSave.getName());
            }
        }
    }
}

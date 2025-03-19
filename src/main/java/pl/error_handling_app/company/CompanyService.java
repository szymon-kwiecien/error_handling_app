package pl.error_handling_app.company;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.user.UserService;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserService userService;

    public CompanyService(CompanyRepository companyRepository, UserService userService) {
        this.companyRepository = companyRepository;
        this.userService = userService;
    }

    public List<CompanyDto> findALlCompanies() {
        return companyRepository.findAll().stream().map(CompanyDtoMapper::map).toList();
    }

    public void saveCompany(CompanyDto companyToSave) {
        validateCompany(companyToSave, null);
        Company company = CompanyDtoMapper.map(companyToSave);
        companyRepository.save(company);
    }

    @Transactional
    public void updateCompany(CompanyDto companyToUpdate, Long companyId) {
        validateCompany(companyToUpdate,companyId);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Firma nie została znaleziona."));
        company.setName(companyToUpdate.getName());
        company.setTimeToFirstRespond(companyToUpdate.getTimeToFirstRespond());
        company.setTimeToResolve(companyToUpdate.getTimeToResolve());
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        Company companyToDelete = companyRepository.findById(companyId)
                .orElseThrow(() -> new NoSuchElementException("Firma nie została znaleziona."));
        companyToDelete.getUsers().forEach(userService::detachUserFromReports); //Gdy usuwamy firmę to jednocześnie usuwamy jej wszystkich pracowników
        //ale aby to zrobić muszę najpierw odłączyć ich od zgłoszeń które obsługują oraz usunąć zgłoszenia które oni utworzyli
        companyRepository.delete(companyToDelete);
    }

    private void validateCompany(CompanyDto companyDto, Long companyToUpdateId) {
        if (companyDto.getTimeToFirstRespond() < 1 || companyDto.getTimeToFirstRespond() > 48) {
            throw new IllegalArgumentException("Czas na pierwszą reakcję musi wynosić pomiędzy 1 a 48 godzin.");
        }
        if (companyDto.getTimeToResolve() < 1 || companyDto.getTimeToResolve() > 168) {
            throw new IllegalArgumentException("Czas na rozwiązanie musi wynosić pomiędzy 1 a 168 godzin.");
        }
        if (companyRepository.findByName(companyDto.getName()).isPresent()) {
            Long existingCompanyId = companyRepository.findByName(companyDto.getName()).get().getId();
            if (companyToUpdateId == null || !existingCompanyId.equals(companyToUpdateId)) {
                throw new IllegalArgumentException("Firma o podanej nazwie już istnieje: " + companyDto.getName());
            }
        }
    }
}

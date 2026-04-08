package pl.error_handling_app.company.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.company.dto.CompanyDto;
import pl.error_handling_app.company.dto.CompanyDtoMapper;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.company.repository.CompanyRepository;
import pl.error_handling_app.exception.CompanyAlreadyExistsException;
import pl.error_handling_app.exception.CompanyNotFoundException;
import pl.error_handling_app.user.service.UserService;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserService userService;

    public CompanyService(CompanyRepository companyRepository, UserService userService) {
        this.companyRepository = companyRepository;
        this.userService = userService;
    }

    public List<CompanyDto> findAllCompanies() {
        return companyRepository.findAll().stream().map(CompanyDtoMapper::map).toList();
    }

    public Page<CompanyDto> findPagedCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable).map(CompanyDtoMapper::map);
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
                .orElseThrow(() -> new CompanyNotFoundException("Firma nie została znaleziona."));
        company.setName(companyToUpdate.name());
        company.setTimeToFirstRespond(companyToUpdate.timeToFirstRespond());
        company.setTimeToResolve(companyToUpdate.timeToResolve());
    }

    @Transactional
    public void deleteCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("Firma nie została znaleziona.");
        }
        userService.prepareUsersForCompanyDeletion(companyId);//Gdy usuwamy firmę to jednocześnie usuwamy jej wszystkich pracowników
        //ale aby to zrobić muszę najpierw odłączyć ich od zgłoszeń które obsługują oraz usunąć zgłoszenia które oni utworzyli
        companyRepository.deleteById(companyId);
    }

    private void validateCompany(CompanyDto companyDto, Long companyToUpdateId) {
        companyRepository.findByName(companyDto.name())
                .ifPresent(existingCompany -> {
                    if (companyToUpdateId == null || !existingCompany.getId().equals(companyToUpdateId)) {
                        throw new CompanyAlreadyExistsException("Firma o nazwie %s już istnieje".formatted(companyDto.name()));
                    }
                });
    }
}

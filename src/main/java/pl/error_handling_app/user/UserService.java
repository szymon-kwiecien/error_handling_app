package pl.error_handling_app.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.error_handling_app.company.Company;
import pl.error_handling_app.company.CompanyRepository;
import pl.error_handling_app.exception.UserAlreadyExistsException;
import pl.error_handling_app.report.Report;
import pl.error_handling_app.report.ReportRepository;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserDtoMapper mapper;
    private final ReportRepository reportRepository;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository, UserRoleRepository userRoleRepository, UserDtoMapper mapper, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userRoleRepository = userRoleRepository;
        this.mapper = mapper;
        this.reportRepository = reportRepository;
    }

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream().map(UserDtoMapper::map).toList();
    }

    public void addUser(UserDto newUser) {
            checkUserAlreadyExists(newUser.getEmail());
            User userToSave = mapper.map(newUser);
            userRepository.save(userToSave);

    }

    @Transactional
    public void updateUser(Long userId, UserEditDto userDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Nie znaleziono użytkownika."));
        userRepository.findByEmail(userDto.getEmail()).ifPresent(foundUser -> {
            if(!foundUser.getId().equals(userId))
                throw new IllegalArgumentException("Taki adres e-mail posiada już inny użytkownik.");
        });

        Company company = companyRepository.findById(userDto.getCompanyId())
                .orElseThrow(() -> new NoSuchElementException("Wybrana firma nie została znaleziona."));
        UserRole role = userRoleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new NoSuchElementException("Wybrana rola nie została znaleziona"));

        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(userDto.getPassword()); //Po dodaniu Spring security muszę to zmienić
        user.setCompany(company);
        Set<UserRole> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
    }

        @Transactional
        public void deleteUser(Long userId) {
            User userToDelete = userRepository.findById(userId).orElseThrow(NoSuchElementException::new);
            detachUserFromReports(userToDelete);
            userRepository.deleteById(userId);
        }

        @Transactional
        public void detachUserFromReports(User userToDelete) {
        reportRepository.findAllByAssignedEmployee(userToDelete).forEach(report -> report.setAssignedEmployee(null));
        List<Report> reportsToDelete = reportRepository.findAllByReportingUser(userToDelete); //Gdy usuwamy użytkownika to jego zgłoszenia również usuwamy
        reportRepository.deleteAll(reportsToDelete);
    }

    private void checkUserAlreadyExists(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Użytkownik %s już istnieje!".formatted(email));
        }
    }
}

package pl.error_handling_app.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.company.repository.CompanyRepository;
import pl.error_handling_app.exception.*;
import pl.error_handling_app.report.service.ReportService;
import pl.error_handling_app.user.dto.UserDto;
import pl.error_handling_app.user.dto.UserDtoMapper;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;
import pl.error_handling_app.user.entity.VerificationToken;
import pl.error_handling_app.user.repository.UserRepository;
import pl.error_handling_app.user.repository.UserRoleRepository;
import pl.error_handling_app.user.repository.VerificationTokenRepository;
import pl.error_handling_app.utils.SecurityUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private ReportService reportService;
    @Mock private UserRoleRepository roleRepository;
    @Mock private UserDtoMapper mapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserPasswordChangeOrActiveService userPasswordChangeOrActiveService;
    @Mock private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldGetUserFirstName() {
        //given
        given(userRepository.findFirstNameByEmail("test@fixaro.pl")).willReturn(Optional.of("Jan"));

        //when
        String firstName = userService.getUserFirstName("test@fixaro.pl");

        //then
        assertThat(firstName).isEqualTo("Jan");
    }

    @Test
    void shouldThrowExceptionWhenUserFirstNameNotFound() {
        //given
        given(userRepository.findFirstNameByEmail("brak@fixaro.pl")).willReturn(Optional.empty());

        //when,then
        assertThrows(UserNotFoundException.class, () -> userService.getUserFirstName("brak@fixaro.pl"));
    }

    @Test
    void shouldFindPagedUsers() {
        //given
        PageRequest pageable = PageRequest.of(0, 10);
        User user = new User();
        Page<User> userPage = new PageImpl<>(List.of(user));

        given(userRepository.findAll(pageable)).willReturn(userPage);
        given(mapper.map(user)).willReturn(new UserDto(1L, "Jan", "Kowalski", "email", 1L, 1L, true));

        //when
        Page<UserDto> result = userService.findPagedUsers(pageable);

        //then
        assertThat(result).hasSize(1);
        then(mapper).should().map(user);
    }

    @Test
    void shouldAddUserSuccessfully() {
        //given
        UserDto dto = new UserDto(null, "Jan", "Kowalski", "nowy@fixaro.pl", 10L, 20L, true);
        Company company = new Company();
        UserRole role = new UserRole();
        User mappedUser = new User();

        given(userRepository.findByEmail("nowy@fixaro.pl")).willReturn(Optional.empty());
        given(companyRepository.findById(10L)).willReturn(Optional.of(company));
        given(roleRepository.findById(20L)).willReturn(Optional.of(role));
        given(mapper.map(dto, company, role)).willReturn(mappedUser);

        //when
        userService.addUser(dto);

        //then
        then(userRepository).should().save(mappedUser);
        then(userPasswordChangeOrActiveService).should().createVerificationToken(mappedUser);
    }

    @Test
    void shouldThrowExceptionWhenAddingUserWithExistingEmail() {
        //given
        UserDto dto = new UserDto(null, "Jan", "Kowalski", "zajety@fixaro.pl", 10L, 20L, true);
        given(userRepository.findByEmail("zajety@fixaro.pl")).willReturn(Optional.of(new User()));

        //when,then
        assertThrows(UserAlreadyExistsException.class, () -> userService.addUser(dto));

        then(userRepository).should(never()).save(any());
    }

    @Test
    void shouldUpdateOtherUserSuccessfully() {
        //given
        Long targetUserId = 5L;
        String currentUserEmail = "admin@fixaro.pl";
        UserDto updateDto = new UserDto(targetUserId, "Jan", "Kowalski", "nowy-email@fixaro.pl", 10L, 20L, true);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setEmail("stary-email@fixaro.pl");

        Company company = new Company();
        UserRole role = new UserRole();
        role.setName("ROLE_EMPLOYEE");

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        given(userRepository.findByEmail("nowy-email@fixaro.pl")).willReturn(Optional.empty()); // Nowy mail jest wolny
        given(companyRepository.findById(10L)).willReturn(Optional.of(company));
        given(roleRepository.findById(20L)).willReturn(Optional.of(role));

        //when
        userService.updateUser(targetUserId, updateDto, currentUserEmail);

        //then
        assertThat(targetUser.getEmail()).isEqualTo("nowy-email@fixaro.pl");
        then(mapper).should().updateEntity(targetUser, updateDto, company, role);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingSelfWithDifferentEmail() {
        //given
        Long myId = 1L;
        String myEmail = "ja@fixaro.pl";
        UserDto updateDto = new UserDto(myId, "Jan", "Kowalski", "zmieniony-email@fixaro.pl", 10L, 20L, true);

        User myAccount = new User();
        myAccount.setId(myId);
        myAccount.setEmail(myEmail);

        given(userRepository.findById(myId)).willReturn(Optional.of(myAccount));

        //when,then
        assertThrows(UnauthorizedOperationException.class, () ->
                userService.updateUser(myId, updateDto, myEmail)
        );
    }

    @Test
    void shouldThrowExceptionWhenRemovingOwnAdminRole() {
        //given
        Long myId = 1L;
        String myEmail = "admin@fixaro.pl";
        UserDto updateDto = new UserDto(myId, "Jan", "Kowalski", myEmail, 10L, 20L, true); // Chcę zmienić sobie rolę

        UserRole adminRole = new UserRole();
        adminRole.setName(SecurityUtils.ADMIN_ROLE);

        UserRole employeeRole = new UserRole();
        employeeRole.setName("ROLE_EMPLOYEE");

        User myAccount = new User();
        myAccount.setId(myId);
        myAccount.setEmail(myEmail);
        myAccount.setRoles(Set.of(adminRole));

        given(userRepository.findById(myId)).willReturn(Optional.of(myAccount));
        given(companyRepository.findById(10L)).willReturn(Optional.of(new Company()));
        given(roleRepository.findById(20L)).willReturn(Optional.of(employeeRole));

        //when then
        assertThrows(UnauthorizedOperationException.class, () ->
                userService.updateUser(myId, updateDto, myEmail)
        );
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        //given
        Long targetId = 5L;
        String currentUserEmail = "admin@fixaro.pl";

        User targetUser = new User();
        targetUser.setId(targetId);
        targetUser.setEmail("pracownik@fixaro.pl");

        UserRole employeeRole = new UserRole();
        employeeRole.setName("ROLE_EMPLOYEE");
        targetUser.setRoles(Set.of(employeeRole));

        VerificationToken token = new VerificationToken();

        given(userRepository.findById(targetId)).willReturn(Optional.of(targetUser));
        given(verificationTokenRepository.findByUser(targetUser)).willReturn(Optional.of(token));

        //when
        userService.deleteUser(targetId, currentUserEmail);

        //then
        then(reportService).should().handleUserRemoval(targetUser);
        then(verificationTokenRepository).should().delete(token);
        then(userRepository).should().deleteById(targetId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingSelf() {
        //given
        Long myId = 1L;
        String myEmail = "ja@fixaro.pl";

        User myAccount = new User();
        myAccount.setEmail(myEmail);

        given(userRepository.findById(myId)).willReturn(Optional.of(myAccount));

        //when,then
        assertThrows(UnauthorizedOperationException.class, () -> userService.deleteUser(myId, myEmail));
        then(userRepository).should(never()).deleteById(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingAnotherAdmin() {
        //given
        Long targetId = 2L;
        String currentUserEmail = "admin1@fixaro.pl";

        User targetAdmin = new User();
        targetAdmin.setEmail("admin2@fixaro.pl");

        UserRole adminRole = new UserRole();
        adminRole.setName(SecurityUtils.ADMIN_ROLE);
        targetAdmin.setRoles(Set.of(adminRole));

        given(userRepository.findById(targetId)).willReturn(Optional.of(targetAdmin));

        //when,then
        assertThrows(UnauthorizedOperationException.class, () -> userService.deleteUser(targetId, currentUserEmail));
        then(userRepository).should(never()).deleteById(any());
    }
}
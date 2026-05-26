package pl.error_handling_app.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.error_handling_app.company.entity.Company;
import pl.error_handling_app.user.entity.User;
import pl.error_handling_app.user.entity.UserRole;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoMapperTest {

    private UserDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserDtoMapper();
    }

    @Test
    void shouldMapUserDtoToEntity() {
        //given
        Company company = new Company();
        company.setId(10L);

        UserRole role = new UserRole();
        role.setId(20L);
        role.setName("ROLE_EMPLOYEE");

        UserDto dto = new UserDto(1L, "Jan", "Kowalski", "Jan@fixaro.pl", 10L, 20L, true);

        //when
        User result = mapper.map(dto, company, role);

        //then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Jan");
        assertThat(result.getLastName()).isEqualTo("Kowalski");
        assertThat(result.getEmail()).isEqualTo("Jan@fixaro.pl");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCompany()).isEqualTo(company);
        assertThat(result.getRoles()).containsExactly(role);
    }

    @Test
    void shouldMapEntityToUserDto() {
        //given
        Company company = new Company();
        company.setId(10L);

        UserRole role = new UserRole();
        role.setId(20L);

        User user = new User();
        user.setId(5L);
        user.setFirstName("Ewa");
        user.setLastName("Kowalska");
        user.setEmail("ewa@fixaro.pl");
        user.setActive(false);
        user.setCompany(company);
        user.setRoles(new HashSet<>(Set.of(role)));

        //when
        UserDto result = mapper.map(user);

        //then
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.firstName()).isEqualTo("Ewa");
        assertThat(result.email()).isEqualTo("ewa@fixaro.pl");
        assertThat(result.companyId()).isEqualTo(10L);
        assertThat(result.roleId()).isEqualTo(20L);
        assertThat(result.isActive()).isFalse();
    }

    @Test
    void shouldUpdateEntityFromDto() {
        //given
        User existingUser = new User();
        existingUser.setFirstName("Stare Imię");
        existingUser.setRoles(new HashSet<>());

        Company newCompany = new Company();
        UserRole newRole = new UserRole();
        newRole.setName("ROLE_ADMIN");

        UserDto updateDto = new UserDto(null, "Nowe Imię", "Nowe Nazwisko", "email@test.pl", null, null, null);

        //when
        mapper.updateEntity(existingUser, updateDto, newCompany, newRole);

        //then
        assertThat(existingUser.getFirstName()).isEqualTo("Nowe Imię");
        assertThat(existingUser.getLastName()).isEqualTo("Nowe Nazwisko");
        assertThat(existingUser.getCompany()).isEqualTo(newCompany);
        assertThat(existingUser.getRoles()).containsExactly(newRole);
    }

    @Test
    void shouldSetIsActiveToFalseWhenNullInUserDto() {
        //given
        UserDto dto = new UserDto(1L, "Jan", "Kowalski", "Jan@fixaro.pl", 10L, 20L, null);

        //then
        assertThat(dto.isActive()).isFalse();
    }
}
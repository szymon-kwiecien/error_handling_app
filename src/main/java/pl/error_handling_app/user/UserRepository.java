package pl.error_handling_app.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findALlByRoles_Name(String roleName);
    Optional<User> findByEmailAndIsActiveIsTrue(String email);
    @Query("SELECT u.firstName FROM User u WHERE u.email = :email")
    Optional<String> findFirstNameByEmail(String email);
}

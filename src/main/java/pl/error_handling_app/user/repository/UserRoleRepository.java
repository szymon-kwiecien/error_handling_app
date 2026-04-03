package pl.error_handling_app.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.error_handling_app.user.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}

package pl.error_handling_app.user;

import org.springframework.stereotype.Service;
import pl.error_handling_app.exception.UserAlreadyExistsException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDtoMapper mapper;

    public UserService(UserRepository userRepository, UserDtoMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream().map(UserDtoMapper::map).toList();
    }

    public void addUser(UserDto newUser) {
            checkUserAlreadyExists(newUser.getEmail());
            User userToSave = mapper.map(newUser);
            userRepository.save(userToSave);

    }

    private void checkUserAlreadyExists(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Użytkownik %s już istnieje!".formatted(email));
        }
    }
}

package ru.project.avito.UserTest.integrationTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import ru.project.user.UserApplication;
import ru.project.user.dao.UserRepository;
import ru.project.user.dto.CreateUserDto;
import ru.project.user.dto.UserDto;
import ru.project.user.exceptions.EmailConflictException;
import ru.project.user.exceptions.NotFoundException;
import ru.project.user.feign.ItemClientForUsers;
import ru.project.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(classes = UserApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("Интеграционные тесты UserService")
public class UserServiceIT extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ItemClientForUsers itemClient;

    @Test
    @DisplayName("Успешный цикл CRUD операций")
    void CRUD_Check() {
        CreateUserDto create = new CreateUserDto("Дмитрий", "email@mail.ru");
        UserDto savedUser = userService.createUser(create);

        assertNotNull(savedUser.id());
        assertEquals(1, userRepository.count());
        assertEquals("Дмитрий", savedUser.name());

        UserDto userById = userService.getUserById(savedUser.id());
        assertEquals(savedUser.id(), userById.id());
        assertEquals("email@mail.ru", userById.email());

        UserDto requestToUpdate = new UserDto(savedUser.id(), "NewДмитрий", "email@mail.ru");
        UserDto updated = userService.update(savedUser.id(), requestToUpdate);
        assertEquals("NewДмитрий", updated.name());
        assertEquals(1, userRepository.count());

        userService.deleteById(savedUser.id());
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("Выброс EmailConflictException при попытке сохранить дубликат почты в базу данных")
    void conflictEmail() {
        CreateUserDto createUser = new CreateUserDto("name","doubleEmial@mail.com");
        userService.createUser(createUser);
        CreateUserDto doubleUser = new CreateUserDto("name", "doubleEmial@mail.com");
        assertThrows(EmailConflictException.class, () -> userService.createUser(doubleUser));
    }

    @Test
    @DisplayName("Выброс NotFoundException при поиске несуществующего ID")
    void findNotExistUser() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }
}

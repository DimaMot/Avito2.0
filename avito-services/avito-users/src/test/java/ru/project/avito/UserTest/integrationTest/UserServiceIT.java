package ru.project.avito.UserTest.integrationTest;

import jakarta.persistence.EntityManager;
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
import ru.project.user.model.User;
import ru.project.user.service.UserService;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты UserService")
public class UserServiceIT extends BaseIntegrationTest {
    private final long userTestId = 1L;
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ItemClientForUsers itemClient;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Успешное создание пользователя")
    void createUser() {
        CreateUserDto create = new CreateUserDto("Дмитрий", "email@mail.ru");
        UserDto savedUser = userService.createUser(create);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(savedUser.id());
        assertEquals(1, userRepository.count());
        assertEquals("Дмитрий", savedUser.name());
    }

    @Test
    @DisplayName("Успешное получение пользователя")
    void getUserById() {
        User user = userRepository.saveAndFlush(new User(userTestId, "dima", "dima@mail.ru"));
        entityManager.clear();

        UserDto userById = userService.getUserById(user.getId());
        assertEquals(user.getId(), userById.id());
        assertEquals("dima@mail.ru", userById.email());
    }

    @Test
    @DisplayName("Успешное обновление пользователя")
    void updateUser() {
        User user = userRepository.saveAndFlush(new User(userTestId, "dima", "dima@mail.ru"));
        entityManager.clear();

        UserDto requestToUpdate = new UserDto(user.getId(), "NewДмитрий", "email@mail.ru");
        UserDto updated = userService.update(user.getId(), requestToUpdate);
        entityManager.flush();
        entityManager.clear();

        assertEquals("NewДмитрий", updated.name());
        assertEquals(1, userRepository.count());
    }

    @Test
    @DisplayName("Успешное удаление пользователя")
    void deleteUser() {
        User user = userRepository.saveAndFlush(new User(userTestId, "dima", "dima@mail.ru"));
        entityManager.clear();

        userService.deleteById(user.getId());
        entityManager.flush();
        entityManager.clear();

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

package ru.project.avito.UserTest.unitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.project.user.dao.UserRepository;
import ru.project.user.dto.CreateUserDto;
import ru.project.user.dto.UserDto;
import ru.project.user.dto.mapper.UserMapper;
import ru.project.user.exceptions.EmailConflictException;
import ru.project.user.exceptions.NotFoundException;
import ru.project.user.feign.ItemClientForUsers;
import ru.project.user.model.User;
import ru.project.user.service.UserServiceImpl;
import ru.project.user.service.update.UserFieldUpdate;
import ru.project.user.service.validate.UserValidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование UserServiceImpl")
public class UserServiceTest {
    private final long testUserId = 1;
    private final String testName = "Dimooon";
    private final String testEmail = "dmitrii-motorin@mail.ru";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidation userValidation;

    @Mock
    private UserFieldUpdate emailFieldUpdate;

    @Mock
    private ItemClientForUsers itemClient;

    @Mock
    private UserFieldUpdate nameFieldUpdate;

    @Spy
    private List<UserFieldUpdate> fieldUpdates = new ArrayList<>();

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        fieldUpdates.clear();
        fieldUpdates.add(emailFieldUpdate);
        fieldUpdates.add(nameFieldUpdate);
    }

    @Test
    @DisplayName("Успешное получение пользователя по ID")
    void getUserById_WhenUserExist_ReturnTypeUserDto() {
        User user = new User(testUserId, testName, testEmail);
        UserDto expectedDto = new UserDto(testUserId, testName, testEmail);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(expectedDto);

        UserDto resultDto = userService.getUserById(testUserId);

        assertNotNull(resultDto);
        assertEquals(expectedDto.id(), resultDto.id());
        assertEquals(expectedDto.name(), resultDto.name());
        assertEquals(expectedDto.email(), resultDto.email());

        verify(userRepository, times(1)).findById(testUserId);
        verify(userMapper, times(1)).toUserDto(user);

        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Выброс исключения NotFoundException при получении несуществующего пользователя")
    void getUserById_WhenUserDoesntExist_ReturnTypeNotFoundEx() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(testUserId));

        assertEquals("Пользователь с id " + testUserId + " не найден", exception.getMessage());

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Успешное создание пользователя")
    void creatUser_WhenUserDoesntExist_ReturnTypeUserDto() {
        CreateUserDto createUserDto = new CreateUserDto(testName, testEmail);
        User user = new User(testUserId, testName, testEmail);
        UserDto expectedDto = new UserDto(testUserId, testName, testEmail);

        when(userMapper.toUser(createUserDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDto(user)).thenReturn(expectedDto);

        UserDto resultDto = userService.createUser(createUserDto);

        assertNotNull(resultDto);
        assertEquals(expectedDto.id(), resultDto.id());
        assertEquals(expectedDto.name(), resultDto.name());
        assertEquals(expectedDto.email(), resultDto.email());

        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUser(createUserDto);
        verify(userMapper, times(1)).toUserDto(user);

        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    @DisplayName("Выброс исключения EmailConflictException при создании пользователя с сущ. email")
    void createUser_WhenExistEmail_ReturnTypeEmailConflictEx() {
        CreateUserDto createUserDto = new CreateUserDto(testName, testEmail);
        doThrow(new EmailConflictException("Почта уже существует" + createUserDto.email()))
                .when(userValidation).existEmail(createUserDto.email());

        final EmailConflictException exception = assertThrows(EmailConflictException.class,
                () -> userService.createUser(createUserDto));

        assertTrue(exception.getMessage().contains(testEmail));
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Успешное удаление пользователя")
    void deleteUserById_WhenUserExist() {
        User user = new User(testUserId, testName, testEmail);

        when(userRepository.findById(testUserId))
                .thenReturn(Optional.of(user));
        userService.deleteById(testUserId);

        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).delete(user);

        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Выброс исключения NotFoundException при удалении пользователя")
    void deleteUserById_WhenUserDoesNotExist_ReturnTypeNotFoundEx() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteById(testUserId));

        assertEquals("Пользователь с id " + testUserId + " не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).delete(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Успешное обновление пользователя")
    void update_WhenUserExist_ReturnTypeUserDtoUpdated() {
        User oldUser = new User(testUserId, "oldName", "oldEmail");
        UserDto updateRequest = new UserDto(testUserId, testName, testEmail);
        User updated = new User(testUserId, testName, testEmail);
        UserDto expected = new UserDto(testUserId, testName, testEmail);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(oldUser));
        when(userRepository.saveAndFlush(oldUser)).thenReturn(updated);
        when(userMapper.toUserDto(updated)).thenReturn(expected);

        when(emailFieldUpdate.support(updateRequest)).thenReturn(true);
        when(nameFieldUpdate.support(updateRequest)).thenReturn(true);

        UserDto result = userService.update(testUserId, updateRequest);

        assertNotNull(result);
        assertEquals(expected.id(), result.id());
        assertEquals(expected.name(), result.name());
        assertEquals(expected.email(), result.email());

        verify(emailFieldUpdate, times(1)).support(updateRequest);
        verify(emailFieldUpdate, times(1)).update(oldUser, updateRequest);
        verify(nameFieldUpdate, times(1)).support(updateRequest);
        verify(nameFieldUpdate, times(1)).update(oldUser, updateRequest);

        verify(userRepository, times(1)).saveAndFlush(oldUser);
        verify(userMapper, times(1)).toUserDto(updated);
    }
}

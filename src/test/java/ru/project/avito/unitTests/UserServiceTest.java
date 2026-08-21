package ru.project.avito.unitTests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.project.avito.exceptions.EmailConflictException;
import ru.project.avito.exceptions.NotFoundException;
import ru.project.avito.user.UserRepository;
import ru.project.avito.user.UserServiceImpl;
import ru.project.avito.user.dto.CreateUserDto;
import ru.project.avito.user.dto.UserDto;
import ru.project.avito.user.dto.mapper.UserMapper;
import ru.project.avito.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование UserServiceImpl")
public class UserServiceTest {
    private final long testUserId = 1;
    private final String testName = "dima";
    private final String testEmail = "dmitrii-motorin@mail.ru";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

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
    @DisplayName("Успешное создание пользователя")
    void creatUser_WhenUserDoesntExist_ReturnTypeUserDto() {
        CreateUserDto createUserDto = new CreateUserDto(testName, testEmail);
        User user = new User(testUserId, testName, testEmail);
        UserDto expectedDto = new UserDto(testUserId, testName, testEmail);

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
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
    void createUserWhenExistEmail_ReturnTypeEmailConflictException() {
        CreateUserDto createUserDto = new CreateUserDto(testName, testEmail);

        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        final EmailConflictException exception = assertThrows(EmailConflictException.class,
                () -> userService.createUser(createUserDto));

        assertTrue(exception.getMessage().contains(testEmail));
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Выьрос исключения NotFoundException при получении несуществующего пользователя")
    void getUserById_WhenUserDoesntExist_ReturnTypeNotFoundException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(testUserId));

        assertEquals("Пользователь с id " + testUserId + " не найден", exception.getMessage());

        verifyNoMoreInteractions(userRepository);
    }
}

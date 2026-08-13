package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.EmailConflictException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidatedException;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto getUserById(long userId) {
        log.info("Получение пользователя по id {}", userId);
        return userRepository.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Получение списка пользователей");
        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto createUser(CreateUserDto user) {
        log.info("Создание пользователя");
        if (user != null) {
            existByEmail(user.getEmail());
            User createdUser = UserMapper.toUser(user);
            return UserMapper.toUserDto(userRepository.create(createdUser));
        }

        throw new ValidatedException("Пользователь должен быть инициализован");
    }

    @Override
    public void deleteById(long userId) {
        log.info("Удаление пользователя с id {}", userId);
        User user = userRepository.delete(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        log.info("Пользователь c id {} удален", userId);
    }

    @Override
    public UserDto update(UserDto updateUser) {
        log.info("Обновление пользователя");
        User oldUser = userRepository.findById(updateUser.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + updateUser.getId() + " не найден"));
        if (updateUser.getName() != null) {
            oldUser.setName(updateUser.getName());
        }
        if (updateUser.getEmail() != null && !(updateUser.getEmail().equals(oldUser.getEmail()))) {
            if (updateUser.getEmail().isBlank() || !updateUser.getEmail().contains("@")) {
                throw new ValidatedException("Неправильный формат почты");
            }
            existByEmail(updateUser.getEmail());
            oldUser.setEmail(updateUser.getEmail());
        }
        log.info("Пользователь обновлен {}", oldUser);
        return UserMapper.toUserDto(oldUser);
    }

    private void existByEmail(String email) {
        if (userRepository.existByEmail(email)) {
            log.warn("Отмена обновления/создания пользователя, email {} уже существует", email);
            throw new EmailConflictException("Пользователь с " + email + " уже существует");
        }
    }
}

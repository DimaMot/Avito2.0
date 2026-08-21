package ru.project.avito.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.project.avito.exceptions.EmailConflictException;
import ru.project.avito.exceptions.NotFoundException;
import ru.project.avito.exceptions.ValidatedException;
import ru.project.avito.user.dto.CreateUserDto;
import ru.project.avito.user.dto.UserDto;
import ru.project.avito.user.dto.mapper.UserMapper;
import ru.project.avito.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getUserById(long userId) {
        log.info("Получение пользователя по id {}", userId);
        return userRepository.findById(userId)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Получение списка пользователей");
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserDto user) {
        log.info("Создание пользователя");
        if (user != null) {
            existByEmail(user.email());
            User createdUser = userMapper.toUser(user);
            return userMapper.toUserDto(userRepository.save(createdUser));
        }

        throw new ValidatedException("Пользователь должен быть инициализован");
    }

    @Override
    @Transactional
    public void deleteById(long userId) {
        log.info("Удаление пользователя с id {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        userRepository.delete(user);
        log.info("Пользователь c id {} удален", userId);
    }

    @Override
    @Transactional
    public UserDto update(long userId, UserDto updateUser) {
        log.info("Обновление пользователя");
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        if (updateUser.name() != null) {
            oldUser.setName(updateUser.name());
        }
        if (updateUser.email() != null && !(updateUser.email().equals(oldUser.getEmail()))) {
            if (updateUser.email().isBlank() || !updateUser.email().contains("@")) {
                throw new ValidatedException("Неправильный формат почты");
            }
            existByEmail(updateUser.email());
            oldUser.setEmail(updateUser.email());
        }
        log.info("Пользователь обновлен {}", oldUser);
        return userMapper.toUserDto(oldUser);
    }

    private void existByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Отмена создание/обновления почта {} существует", email);
            throw new EmailConflictException("Почта уже существует" + email);
        }
    }
}

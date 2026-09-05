package ru.project.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.project.user.dao.UserRepository;
import ru.project.user.dto.CreateUserDto;
import ru.project.user.dto.UserDto;
import ru.project.user.dto.mapper.UserMapper;
import ru.project.user.exceptions.NotFoundException;
import ru.project.user.exceptions.ValidatedException;
import ru.project.user.feign.ItemClientForUsers;
import ru.project.user.model.User;
import ru.project.user.service.update.UserFieldUpdate;
import ru.project.user.service.validate.UserValidation;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidation userValidation;
    private final List<UserFieldUpdate> fieldUpdates;
    private final ItemClientForUsers itemClient;

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
        if (user == null) {
            throw new ValidatedException("Пользователь должен быть инициализован");
        }
        userValidation.existEmail(user.email());
        User createdUser = userRepository.save(userMapper.toUser(user));
        log.info("Пользователь с id {} успешно создан", createdUser.getId());
        return userMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional
    public void deleteById(long userId) {
        log.info("Удаление пользователя с id {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        try {
            itemClient.deleteAllById(userId);
            log.info("Сетевой запрос на удаление вещей пользователя {}", userId);
        } catch (Exception e) {
            log.error("Не удалось удалить предметы пользователя {} по сети: {}", userId, e.getMessage());
            throw new RuntimeException("Ошибка удаления предметов при удалении пользователя");
        }

        userRepository.delete(user);
        log.info("Пользователь c id {} удален", userId);
    }

    @Override
    @Transactional
    public UserDto update(long userId, UserDto updateUser) {
        log.info("Обновление пользователя");
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        fieldUpdates.stream()
                .filter(field -> field.support(updateUser))
                .forEach(field -> field.update(oldUser, updateUser));

        User user = userRepository.saveAndFlush(oldUser);
        log.info("Пользователь обновлен {}", user);
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUsersByIds(List<Long> userIds) {
        log.info("Получение пакета пользователей {}", userIds);
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findAllUsersByIds(userIds);
    }
}

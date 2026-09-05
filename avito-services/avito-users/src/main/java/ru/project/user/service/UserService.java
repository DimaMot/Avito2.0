package ru.project.user.service;

import ru.project.user.dto.CreateUserDto;
import ru.project.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUserById(long userId);

    List<UserDto> getAll();

    UserDto createUser(CreateUserDto user);

    void deleteById(long userId);

    UserDto update(long userId, UserDto user);

    List<UserDto> getUsersByIds(List<Long> userIds);
}

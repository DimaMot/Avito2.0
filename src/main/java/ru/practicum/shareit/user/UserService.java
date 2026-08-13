package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUserById(long userId);

    List<UserDto> getAll();

    UserDto createUser(CreateUserDto user);

    void deleteById(long userId);

    UserDto update(UserDto user);
}

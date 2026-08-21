package ru.project.avito.user;

import ru.project.avito.user.dto.CreateUserDto;
import ru.project.avito.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUserById(long userId);

    List<UserDto> getAll();

    UserDto createUser(CreateUserDto user);

    void deleteById(long userId);

    UserDto update(long userId, UserDto user);
}

package ru.project.user.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.project.user.dto.CreateUserDto;
import ru.project.user.dto.UserDto;
import ru.project.user.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(CreateUserDto createUserDto);

    UserDto toUserDto(User user);
}

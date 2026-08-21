package ru.project.avito.user.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.project.avito.user.dto.CreateUserDto;
import ru.project.avito.user.dto.UserDto;
import ru.project.avito.user.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(CreateUserDto createUserDto);

    UserDto toUserDto(User user);
}

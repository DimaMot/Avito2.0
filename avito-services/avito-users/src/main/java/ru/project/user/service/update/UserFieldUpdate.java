package ru.project.user.service.update;

import ru.project.user.dto.UserDto;
import ru.project.user.model.User;

public interface UserFieldUpdate {
    boolean support(UserDto field);
    void update(User old, UserDto newFields);
}

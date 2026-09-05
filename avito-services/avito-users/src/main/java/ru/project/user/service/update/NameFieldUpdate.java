package ru.project.user.service.update;

import org.springframework.stereotype.Component;
import ru.project.user.dto.UserDto;
import ru.project.user.model.User;

@Component
public class NameFieldUpdate implements UserFieldUpdate {

    @Override
    public boolean support(UserDto field) {
        return field.name() != null;
    }

    @Override
    public void update(User old, UserDto newFields) {
        if (!old.getName().equals(newFields.name())) {
            old.setName(newFields.name());
        }
    }
}

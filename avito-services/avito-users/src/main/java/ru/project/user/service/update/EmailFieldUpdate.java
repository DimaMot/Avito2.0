package ru.project.user.service.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.project.user.service.validate.UserValidation;
import ru.project.user.dto.UserDto;
import ru.project.user.model.User;

@RequiredArgsConstructor
@Component
public class EmailFieldUpdate implements UserFieldUpdate {
    private final UserValidation userValidation;

    @Override
    public boolean support(UserDto field) {
        return field.email() != null;
    }

    @Override
    public void update(User old, UserDto newFields) {
        if (!old.getEmail().equals(newFields.email())) {
            userValidation.emailValidation(newFields.email());
            userValidation.existEmail(newFields.email());
            old.setEmail(newFields.email());
        }
    }
}

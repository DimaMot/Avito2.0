package ru.project.user.service.validate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.project.commonlib.exceptions.ValidatedException;
import ru.project.user.dao.UserRepository;
import ru.project.commonlib.exceptions.EmailConflictException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidation {
    private final UserRepository userRepository;

    public void emailValidation(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            log.info("Неправильный формат почты {}", email);
            throw new ValidatedException("Неправильный формат почты");
        }
    }

    public void existEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Отмена операции: почта {} существует", email);
            throw new EmailConflictException("Почта уже существует " + email);
        }
    }
}

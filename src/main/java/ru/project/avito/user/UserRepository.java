package ru.project.avito.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.project.avito.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}

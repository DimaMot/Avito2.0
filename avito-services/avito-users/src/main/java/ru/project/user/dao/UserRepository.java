package ru.project.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.project.user.dto.UserDto;
import ru.project.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    @Query("""
    SELECT new ru.project.user.dto.UserDto(u.id, u.name, u.email)
    FROM User u
    WHERE u.id IN :userIds
    """)
    List<UserDto> findAllUsersByIds(@Param("userIds") List<Long> userIds);
}

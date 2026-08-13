package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String name; // — имя или логин пользователя;
    @Email(message = "неверный формат email")
    private String email; // — адрес электронной почты
}

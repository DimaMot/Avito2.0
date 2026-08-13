package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {
    @NotBlank(message = "Имя не может быть пустым")
    private String name; // — имя или логин пользователя;
    @Email(message = "неверный формат email")
    @NotNull(message = "Почта не может быть пустой")
    private String email; // — адрес электронной почты
}

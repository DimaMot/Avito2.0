package ru.project.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserDto(
        @NotBlank(message = "Имя не может быть пустым")
        String name, // — имя или логин пользователя;
        @Email(message = "неверный формат email")
        @NotNull(message = "Почта не может быть пустой")
        String email // — адрес электронной почты
) {}

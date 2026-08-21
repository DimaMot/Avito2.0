package ru.project.avito.user.dto;

public record UserDto(
        Long id,
        String name, // — имя или логин пользователя;
        String email // — адрес электронной почты
) {}
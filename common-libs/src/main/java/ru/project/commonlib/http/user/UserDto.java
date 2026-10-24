package ru.project.commonlib.http.user;

public record UserDto(
        Long id,
        String name, // — имя или логин пользователя;
        String email // — адрес электронной почты
) {}
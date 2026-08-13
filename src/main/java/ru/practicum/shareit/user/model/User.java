package ru.practicum.shareit.user.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {
    private Long id; // — уникальный идентификатор пользователя;
    private String name; // — имя или логин пользователя;
    private String email; // — адрес электронной почты (учтите, что два пользователя не могут
    // иметь одинаковый адрес электронной почты).

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

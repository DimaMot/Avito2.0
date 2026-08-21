package ru.project.avito.user.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // — уникальный идентификатор пользователя;

    @Column(name = "name", nullable = false)
    private String name; // — имя или логин пользователя;

    @Column(name = "email", nullable = false)
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

package ru.project.item.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // — уникальный идентификатор вещи;

    @Column(name = "name", nullable = false)
    private String name; // — краткое название;

    @Column(name = "description", nullable = false)
    private String description; // — развёрнутое описание;

    @Column(name = "is_available", nullable = false)
    private Boolean available; // — статус о том, доступна или нет вещь для аренды;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // — владелец вещи;

    @Column(name = "request_id")
    private Long requestId; // — если вещь была создана по запросу другого пользователя

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Comment> comments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        return id != null && id.equals(((Item) o).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

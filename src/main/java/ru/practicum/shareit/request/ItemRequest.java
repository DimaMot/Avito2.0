package ru.practicum.shareit.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;

/**
 * TODO Sprint add-item-requests.
 */

@Getter
@Setter
@ToString
public class ItemRequest {
    private Long id; // — уникальный идентификатор запроса;
    private String description; // — текст запроса, содержащий описание требуемой вещи;
    private User requestor; //  — пользователь, создавший запрос;
    private Instant created; // — дата и время создания запроса.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemRequest)) return false;
        return id != null && id.equals(((ItemRequest) o).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

package ru.practicum.shareit.booking;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Setter
@Getter
@ToString
public class Booking {
    private Long id; //— уникальный идентификатор бронирования;
    private LocalDateTime start; //  — дата и время начала бронирования;
    private LocalDateTime end; // — дата и время конца бронирования;
    private Item item; // — вещь, которую пользователь бронирует;
    private User booker; // — пользователь, который осуществляет бронирование;
    private BookingStatus status; // — статус бронирования.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        return id != null && id.equals(((Booking) o).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

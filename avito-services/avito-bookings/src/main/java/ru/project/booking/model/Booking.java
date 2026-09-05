package ru.project.booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Setter
@Getter
@ToString
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //— уникальный идентификатор бронирования;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime start; //  — дата и время начала бронирования;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime end; // — дата и время конца бронирования;

    @Column(name = "item_id", nullable = false)
    private Long itemId; // — вещь, которую пользователь бронирует;

    @Column(name = "booker_id", nullable = false)
    private Long bookerId; // — пользователь, который осуществляет бронирование;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
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

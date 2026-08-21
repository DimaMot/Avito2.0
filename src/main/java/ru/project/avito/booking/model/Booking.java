package ru.project.avito.booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.project.avito.booking.BookingStatus;
import ru.project.avito.item.model.Item;
import ru.project.avito.user.model.User;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    private Item item; // — вещь, которую пользователь бронирует;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booker_id", nullable = false)
    @ToString.Exclude
    private User booker; // — пользователь, который осуществляет бронирование;

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

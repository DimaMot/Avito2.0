package ru.project.booking.service.search;

import ru.project.booking.model.Booking;
import ru.project.booking.model.BookingState;
import ru.project.booking.service.search.role.BookingUserRole;

import java.time.OffsetDateTime;
import java.util.List;

public interface SearchBookingsByState {

    BookingState getSupportedState();

    List<Booking> getBookingsByState(long userId, BookingUserRole role, OffsetDateTime now);
}

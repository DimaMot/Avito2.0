package ru.project.avito.booking;

import ru.project.avito.booking.dto.BookingCreateDto;
import ru.project.avito.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingCreateDto createDto, long userId);

    BookingResponseDto approveBooking(long bookingId, boolean approved, long userId);

    BookingResponseDto getBookingById(long bookingId, long userId);

    List<BookingResponseDto> getAllBookingByUser(long userId, String stateStr);

    List<BookingResponseDto> getAllBookingByOwner(long ownerId, String stateStr);
}

package ru.project.booking.service;

import ru.project.booking.dto.BookingCreateDto;
import ru.project.booking.dto.BookingResponseDto;
import ru.project.booking.dto.BookingShortDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookingService {
    BookingResponseDto createBooking(BookingCreateDto createDto, long userId);

    BookingResponseDto approveBooking(long bookingId, boolean approved, long userId);

    BookingResponseDto getBookingById(long bookingId, long userId);

    List<BookingResponseDto> getAllBookingByUser(long userId, String stateStr);

    List<BookingResponseDto> getAllBookingByOwner(long ownerId, String stateStr);

    Map<Long, List<BookingShortDto>> getBookingForItems(List<Long> itemIds);

    Optional<BookingShortDto> getBookingForComment(long userId, long itemId, OffsetDateTime currentTime);
}

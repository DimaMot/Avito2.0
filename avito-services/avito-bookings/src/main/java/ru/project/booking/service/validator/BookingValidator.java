package ru.project.booking.service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.project.booking.dao.BookingRepository;
import ru.project.commonlib.exceptions.ForbiddenContentException;
import ru.project.commonlib.exceptions.NotFoundException;
import ru.project.commonlib.exceptions.ValidatedException;
import ru.project.booking.model.Booking;
import ru.project.booking.model.BookingStatus;
import ru.project.commonlib.http.item.ItemDtoForBookingService;
import ru.project.commonlib.http.user.UserDto;

import java.time.Clock;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class BookingValidator {
    private final Clock clock;
    private final BookingRepository bookingRepository;

    public void validateDates(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null || start.isAfter(end) || start.equals(end)) {
            throw new ValidatedException("Некорректные даты бронирования");
        }

        if (start.isBefore(OffsetDateTime.now(clock))) {
            throw new ValidatedException("Старт бронирования не может быть в прошлом");
        }
    }

    public void validateItemAvailable(ItemDtoForBookingService item, long userId, OffsetDateTime start, OffsetDateTime end) {
        if (!item.available()) {
            throw new NotFoundException("Предмет недоступен для бронирования");
        }

        if (item.ownerId() == userId) {
            throw new NotFoundException("Владелец не может забронировать свою же вещь");
        }

        if (bookingRepository.existsOverlappingBooking(item.id(), start, end)) {
            throw new ValidatedException("Вещь уже забронирована на выбранные даты");
        }
    }

    public void validateChangeBookingStatus(Booking booking, ItemDtoForBookingService item, long userId) {
        if (item.ownerId() != userId) {
            throw new ForbiddenContentException("Менять статус может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidatedException("Статус бронирования уже изменен");
        }
    }
}

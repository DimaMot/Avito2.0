package ru.project.booking.service.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.project.booking.dao.BookingRepository;
import ru.project.booking.feign.ItemForBookingClient;
import ru.project.booking.model.Booking;
import ru.project.booking.model.BookingState;
import ru.project.booking.model.BookingStatus;
import ru.project.booking.service.search.role.BookingUserRole;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchBookingByStateStatus implements SearchBookingsByState {
    private final BookingRepository bookingRepository;
    private final ItemForBookingClient itemClient;

    @Override
    public BookingState getSupportedState() {
        return BookingState.WAITING;
    }

    @Override
    public List<Booking> getBookingsByState(long userId, BookingUserRole role, OffsetDateTime now) {
        return List.of();
    }

    public List<Booking> getBookingWithStatus(long userId, BookingUserRole role, BookingStatus status) {
        return switch (role) {
            case OWNER -> {
                List<Long> itemsIds = itemClient.getItemsIdsByUserId(userId);
                if (itemsIds.isEmpty()) {
                    yield List.of();
                }
                yield bookingRepository.findByOwnerIdAndStatusOrderByStartDesc(itemsIds, status);
            }
            case BOOKER -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, status);
        };
    }
}

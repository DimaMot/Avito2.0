package ru.project.booking.service.search;

import org.springframework.stereotype.Component;
import ru.project.booking.model.Booking;
import ru.project.booking.model.BookingState;
import ru.project.booking.model.BookingStatus;
import ru.project.booking.service.search.role.BookingUserRole;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BookingSearchRegistry {
    private final Map<BookingState, SearchBookingsByState> strategies = new EnumMap<>(BookingState.class);
    private final SearchBookingByStateStatus statusStrategy;

    public BookingSearchRegistry(List<SearchBookingsByState> strategyList, SearchBookingByStateStatus statusStrategy) {
        this.statusStrategy = statusStrategy;

        strategyList.forEach(s -> {
            if (s.getSupportedState() != BookingState.WAITING) {
                strategies.put(s.getSupportedState(), s);
            }
        });
    }

    public List<Booking> search(BookingState state, long userId, BookingUserRole role, OffsetDateTime now) {
        if (state == BookingState.WAITING || state == BookingState.REJECTED) {
            BookingStatus status = BookingStatus.valueOf(state.name());
            return statusStrategy.getBookingWithStatus(userId, role, status);
        }

        SearchBookingsByState strategy = strategies.get(state);
        if (strategy == null) {
            throw new IllegalArgumentException("Стратегия поиска не поддерживается: " + state);
        }

        return strategy.getBookingsByState(userId, role, now);
    }
}

package ru.project.item.service.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.project.item.dto.BookingShortDto;
import ru.project.item.dto.ItemCreatDto;
import ru.project.item.exceptions.ValidatedException;
import ru.project.item.feign.BookingClient;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ItemValidation {
    private final BookingClient bookingClient;

    public void initializeItem(ItemCreatDto dto) {
        if (dto == null) {
            throw new ValidatedException("Предмет должен быть инициализирован");
        }
    }
    public void verifyBookingExist(long userId, long itemId, OffsetDateTime now) {
        Optional<BookingShortDto> bookingShortDto = bookingClient.getBookingByBookerId(userId, itemId, now);

        if (bookingShortDto.isEmpty()) {
            throw new ValidatedException("Пользователь " + userId + " не брал предмет " + itemId + " в аренду или она еще не закончилась");
        }
    }
}

package ru.project.item.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.project.item.dto.BookingShortDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@FeignClient(name = "booking-server", url = "http://booking-server:8083")
public interface BookingClient {
    @GetMapping("/bookings/item-link")
    Map<Long, List<BookingShortDto>> getAllBookingForItems(@RequestParam List<Long> itemIds);

    @GetMapping("/bookings/booker")
    Optional<BookingShortDto> getBookingByBookerId(@RequestParam("userId") long bookerId,
                                                   @RequestParam("itemId") long itemId,
                                                   @RequestParam("now") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime now);
}

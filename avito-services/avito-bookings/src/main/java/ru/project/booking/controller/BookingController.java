package ru.project.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.project.booking.dto.BookingCreateDto;
import ru.project.booking.dto.BookingResponseDto;
import ru.project.booking.dto.BookingShortDto;
import ru.project.booking.service.BookingService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_HEADER = "X-Avito-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@Valid @RequestBody BookingCreateDto createDto,
                                            @RequestHeader(USER_HEADER) long userId) {
        return bookingService.createBooking(createDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@PathVariable long bookingId,
                                             @RequestHeader(USER_HEADER) long userId, @RequestParam boolean approved) {
        return bookingService.approveBooking(bookingId, approved, userId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader(USER_HEADER) long userId,
                                         @PathVariable long bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingByBooker(@RequestHeader(USER_HEADER) long userId,
                                                       @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookingByUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingByOwner(@RequestHeader(USER_HEADER) long owner,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllBookingByOwner(owner, state);
    }

    @GetMapping("/item-link")
    Map<Long, List<BookingShortDto>> getAllBookingForItems(@RequestParam List<Long> itemIds) {
        return bookingService.getBookingForItems(itemIds);
    }

    @GetMapping("/booker")
    Optional<BookingShortDto> getBookingForComment(@RequestParam("userId") long userId,
                                                   @RequestParam("itemId") long itemId,
                                                   @RequestParam("now") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime currentTime) {
        return bookingService.getBookingForComment(userId, itemId, currentTime);
    }
}

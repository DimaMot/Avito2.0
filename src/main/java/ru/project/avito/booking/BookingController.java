package ru.project.avito.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.project.avito.booking.dto.BookingCreateDto;
import ru.project.avito.booking.dto.BookingResponseDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String USER_HEADER = "X-Sharer-User-Id";
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
}

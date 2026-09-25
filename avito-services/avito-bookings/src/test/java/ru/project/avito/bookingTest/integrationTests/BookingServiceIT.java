package ru.project.avito.bookingTest.integrationTests;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import ru.project.booking.dao.BookingRepository;
import ru.project.booking.dto.BookingCreateDto;
import ru.project.booking.dto.BookingResponseDto;
import ru.project.booking.dto.ItemDto;
import ru.project.booking.dto.UserDto;
import ru.project.booking.dto.mapper.BookingMapper;
import ru.project.booking.exceptions.NotFoundException;
import ru.project.booking.exceptions.ValidatedException;
import ru.project.booking.feign.ItemForBookingClient;
import ru.project.booking.feign.UserForBookingClient;
import ru.project.booking.model.Booking;
import ru.project.booking.model.BookingStatus;
import ru.project.booking.service.BookingService;
import ru.project.booking.service.validator.BookingValidator;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Интеграционные тесты для BookingService")
public class BookingServiceIT extends BaseIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingMapper bookingMapper;
    @MockBean
    private ItemForBookingClient itemClient;
    @MockBean
    private UserForBookingClient userClient;
    @Autowired
    private BookingValidator validator;
    @SpyBean
    private Clock clock;

    @Autowired
    private EntityManager entityManager;

    private final long testItemId = 1L;
    private final long testUserId = 2L;

    private ItemDto defaultItem;
    private UserDto defaultUser;

    @BeforeEach
    void setUpContext() {
        doReturn(Instant.parse("2026-09-05T12:00:00Z")).when(clock).instant();
        doReturn(ZoneId.of("UTC")).when(clock).getZone();

        defaultItem = new ItemDto(testItemId, "Thing", "good thing", true, 1L);
        defaultUser = new UserDto(testUserId, "dima", "dima@mail.ru");
    }

    @Test
    @DisplayName("Успешное создание бронирования")
    void createBooking() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        when(userClient.getUserById(testUserId)).thenReturn(defaultUser);
        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);

        BookingCreateDto bookingDto = new BookingCreateDto(testItemId, now.plusMinutes(2), now.plusMinutes(10));
        BookingResponseDto result = bookingService.createBooking(bookingDto, testUserId);

        entityManager.flush();
        entityManager.clear();

        assertNotNull(result);
        assertEquals(1, bookingRepository.count());
    }

    @Test
    @DisplayName("Успешное получение бронирования")
    void getBookingById() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Booking book = bookingRepository.saveAndFlush(new Booking(null, now.minusHours(12), now.plusMinutes(12), testItemId, testUserId, BookingStatus.APPROVED));
        entityManager.clear();

        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);
        when(userClient.getUserById(testUserId)).thenReturn(defaultUser);

        BookingResponseDto result = bookingService.getBookingById(book.getId(), testUserId);

        assertNotNull(result);
        assertEquals(result.start().toEpochSecond(), book.getStart().toEpochSecond());
        assertEquals(result.end().toEpochSecond(), book.getEnd().toEpochSecond());
        verify(itemClient, times(1)).getItemById(testItemId);
        verify(userClient, times(1)).getUserById(testUserId);
    }

    @Test
    @DisplayName("Успешное подтверждение бронирования")
    void approveBooking() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Booking book = bookingRepository.saveAndFlush(new Booking(null, now.minusHours(12), now.plusMinutes(12), testItemId, testUserId, BookingStatus.WAITING));
        entityManager.clear();

        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);
        when(userClient.getUserById(testUserId)).thenReturn(defaultUser);

        BookingResponseDto result = bookingService.approveBooking(book.getId(), true, 1L);

        entityManager.flush();
        entityManager.clear();

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.status());
    }

    @Test
    @DisplayName("Успешное получение будущих бронирований (Фильтр FUTURE)")
    void getBookings_StateFuture_ShouldReturnList() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Booking futureBooking = new Booking(null, now.plusDays(1), now.plusDays(7), testItemId, testUserId, BookingStatus.APPROVED);
        bookingRepository.saveAndFlush(futureBooking);
        entityManager.clear();

        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);
        when(userClient.getUserById(anyLong())).thenReturn(defaultUser);

        List<BookingResponseDto> result = bookingService.getAllBookingByUser(testUserId, "FUTURE");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(futureBooking.getStart().toEpochSecond(), result.getFirst().start().toEpochSecond());
    }

    @Test
    @DisplayName("Успешное получение текущих бронирований (Фильтр CURRENT)")
    void getBookings_StateCurrent_ShouldReturnList() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Booking currentBooking = new Booking(null, now.minusHours(2), now.plusHours(2), testItemId, testUserId, BookingStatus.APPROVED);
        bookingRepository.saveAndFlush(currentBooking);
        entityManager.clear();

        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);
        when(userClient.getUserById(anyLong())).thenReturn(defaultUser);

        List<BookingResponseDto> result = bookingService.getAllBookingByUser(testUserId, "CURRENT");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Выброс NotFoundException при попытке чужого пользователя посмотреть детали брони")
    void getBookingById_ByStranger_ThrowsNotFoundException() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Booking book = bookingRepository.saveAndFlush(new Booking(null, now.minusHours(1), now.plusHours(1), testItemId, testUserId, BookingStatus.APPROVED));
        entityManager.clear();
        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);
        when(userClient.getUserById(testUserId)).thenReturn(defaultUser);
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(book.getId(), 999L));
    }

    @Test
    @DisplayName("Выброс ValidationException, если дата окончания брони меньше даты начала")
    void createBooking_WithInvalidDates_ThrowsValidationException() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        BookingCreateDto invalidBooking = new BookingCreateDto(testItemId, now.plusDays(2), now.plusDays(1));

        when(userClient.getUserById(testUserId)).thenReturn(defaultUser);
        when(itemClient.getItemById(testItemId)).thenReturn(defaultItem);

        assertThrows(ValidatedException.class, () -> bookingService.createBooking(invalidBooking, testUserId));
        assertEquals(0, bookingRepository.count());
    }
}

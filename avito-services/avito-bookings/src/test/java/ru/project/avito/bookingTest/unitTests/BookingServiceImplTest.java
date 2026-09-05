package ru.project.avito.bookingTest.unitTests;


import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.project.booking.dao.BookingRepository;
import ru.project.booking.dto.*;
import ru.project.booking.dto.mapper.BookingMapper;
import ru.project.booking.exceptions.NotFoundException;
import ru.project.booking.exceptions.ValidatedException;
import ru.project.booking.feign.ItemForBookingClient;
import ru.project.booking.feign.UserForBookingClient;
import ru.project.booking.model.Booking;
import ru.project.booking.model.BookingState;
import ru.project.booking.model.BookingStatus;
import ru.project.booking.service.BookingServiceImpl;
import ru.project.booking.service.search.BookingSearchRegistry;
import ru.project.booking.service.search.role.BookingUserRole;
import ru.project.booking.service.validator.BookingValidator;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование BookingServiceImpl")
public class BookingServiceImplTest {

    private final long testUserId = 2; // Арендатор
    private final long testOwnerId = 1; // Владелец вещи
    private final long testItemId = 10;
    private final long testBookingId = 100;

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ItemForBookingClient itemClient;
    @Mock
    private UserForBookingClient userClient;
    @Mock
    private BookingValidator validator;
    @Mock
    private BookingSearchRegistry searchRegistry;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-09-05T12:00:00Z"), ZoneId.of("UTC"));

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingCreateDto createDto;
    private ItemDto itemDto;
    private UserDto userDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        createDto = new BookingCreateDto(testItemId, now.plusDays(1), now.plusDays(2));
        itemDto = new ItemDto(testItemId, "Дрель", "Мощная дрель", true, testOwnerId);
        userDto = new UserDto(testUserId, "Ivan", "ivan@mail.ru");

        booking = new Booking();
        booking.setId(testBookingId);
        booking.setItemId(testItemId);
        booking.setBookerId(testUserId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(createDto.start());
        booking.setEnd(createDto.end());
    }

    @Test
    @DisplayName("Успешное создание бронирования")
    void createBooking_Success_ShouldReturnResponseDto() {
        BookingResponseDto expectedResponse = new BookingResponseDto(testBookingId, createDto.start(), createDto.end(), BookingStatus.WAITING, userDto, new ItemShortDto(testItemId, "Дрель"));

        when(itemClient.getItemById(testItemId)).thenReturn(itemDto);
        when(userClient.getUserById(testUserId)).thenReturn(userDto);
        when(bookingMapper.toBooking(createDto, testItemId, testUserId)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.responseDto(booking, userDto, itemDto)).thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.createBooking(createDto, testUserId);

        assertNotNull(result);
        assertEquals(testBookingId, result.id());
        assertEquals(BookingStatus.WAITING, result.status());

        verify(validator, times(1)).validateDates(createDto.start(), createDto.end());
        verify(validator, times(1)).validateItemAvailable(itemDto, userDto, testUserId);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Выброс NotFoundException при создании бронирования на несуществующую вещь")
    void createBooking_WhenItemNotFound_ReturnTypeNotFoundEx() {
        Request request = Request.create(Request.HttpMethod.GET, "/items/" + testItemId, new HashMap<>(), null, new RequestTemplate());
        FeignException.NotFound feignException = new FeignException.NotFound("Предмет не найден", request, null, null);

        when(itemClient.getItemById(testItemId)).thenThrow(feignException);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> bookingService.createBooking(createDto, testUserId));
        assertEquals("Предмет не найден", ex.getMessage());

        verify(validator, times(1)).validateDates(createDto.start(), createDto.end());
        verifyNoInteractions(userClient, bookingRepository);
    }

    @Test
    @DisplayName("Успешное одобрение бронирования владельцем вещи (+)")
    void approveBooking_Approved_ShouldReturnApprovedDto() {
        UserDto bookerDto = new UserDto(testUserId, "Ivan", "ivan@mail.ru");
        BookingResponseDto expectedResponse = new BookingResponseDto(testBookingId, booking.getStart(), booking.getEnd(), BookingStatus.APPROVED, bookerDto, new ItemShortDto(testItemId, "Дрель"));

        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(booking));
        when(itemClient.getItemById(testItemId)).thenReturn(itemDto);
        when(userClient.getUserById(testUserId)).thenReturn(bookerDto);
        when(bookingMapper.responseDto(booking, bookerDto, itemDto)).thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.approveBooking(testBookingId, true, testOwnerId);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());

        verify(validator, times(1)).validateChangeBookingStatus(booking, itemDto, testOwnerId);
    }

    @Test
    @DisplayName("Выброс NotFoundException при одобрении несуществующего бронирования")
    void approveBooking_WhenBookingDoesNotExist_ThrowsNotFoundException() {
        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(testBookingId, true, testOwnerId));

        verifyNoMoreInteractions(itemClient, userClient, validator);
    }

    @Test
    @DisplayName("Успешное получение бронирования по ID автором брони или владельцем (+)")
    void getBookingById_Success_ShouldReturnDto() {
        BookingResponseDto expectedResponse = new BookingResponseDto(testBookingId, booking.getStart(), booking.getEnd(), BookingStatus.WAITING, userDto, new ItemShortDto(testItemId, "Дрель"));

        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(booking));
        when(itemClient.getItemById(testItemId)).thenReturn(itemDto);
        when(userClient.getUserById(testUserId)).thenReturn(userDto);
        when(bookingMapper.responseDto(booking, userDto, itemDto)).thenReturn(expectedResponse);

        BookingResponseDto result = bookingService.getBookingById(testBookingId, testUserId);

        assertNotNull(result);
        verify(bookingRepository, times(1)).findById(testBookingId);
    }

    @Test
    @DisplayName("Выброс NotFoundException, если посторонний пользователь пытается посмотреть бронь")
    void getBookingById_WhenUserIsHacker_ThrowsNotFoundException() {
        long hackerId = 999L;

        when(bookingRepository.findById(testBookingId)).thenReturn(Optional.of(booking));
        when(itemClient.getItemById(testItemId)).thenReturn(itemDto);
        when(userClient.getUserById(testUserId)).thenReturn(userDto);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(testBookingId, hackerId));
    }

    @Test
    @DisplayName("Успешный проброс параметров в реестр поиска для арендатора")
    void getAllBookingByUser_Success_ShouldTriggerRegistry() {
        when(userClient.getUserById(testUserId)).thenReturn(userDto);
        when(searchRegistry.search(eq(BookingState.ALL), eq(testUserId), eq(BookingUserRole.BOOKER), any(OffsetDateTime.class)))
                .thenReturn(List.of(booking));

        when(itemClient.getItemsByIds(anyList())).thenReturn(List.of(itemDto));
        when(userClient.getUsersByIds(anyList())).thenReturn(List.of(userDto));

        List<BookingResponseDto> result = bookingService.getAllBookingByUser(testUserId, "ALL");

        assertNotNull(result);
        verify(searchRegistry, times(1)).search(eq(BookingState.ALL), eq(testUserId), eq(BookingUserRole.BOOKER), any(OffsetDateTime.class));
    }

    @Test
    @DisplayName("Выброс ValidatedException при передаче битого текстового статуса состояния")
    void getAllBookingByUser_WhenStateIsInvalid_ThrowsValidatedException() {
        assertThrows(ValidatedException.class, () -> bookingService.getAllBookingByUser(testUserId, "INVALID_STATE_STRING"));
        verifyNoInteractions(searchRegistry, bookingRepository);
    }
}
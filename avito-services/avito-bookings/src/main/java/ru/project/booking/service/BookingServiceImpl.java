package ru.project.booking.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.project.booking.service.search.BookingSearchRegistry;
import ru.project.booking.service.search.role.BookingUserRole;
import ru.project.booking.service.validator.BookingValidator;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ItemForBookingClient itemClient;
    private final UserForBookingClient userClient;
    private final BookingValidator validator;
    private final BookingSearchRegistry searchRegistry;
    private final Clock clock;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingCreateDto createDto, long userId) {
        log.info("Создание бронирования для пользоватле с id {} и предмета с id {}", userId, createDto.itemId());

        validator.validateDates(createDto.start(), createDto.end());

        ItemDto item = getItemFromOrThrow(createDto.itemId());
        UserDto user = getUserFromOrThrow(userId);

        validator.validateItemAvailable(item, user, userId);

        Booking booking = bookingMapper.toBooking(createDto, item.id(), user.id());
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование создано для пользоватле с id {} и предмета с id {}", userId, item.id());
        return bookingMapper.responseDto(savedBooking, user, item);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(long bookingId, boolean approved, long userId) {
        log.info("Изменение статуса бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        ItemDto item = getItemFromOrThrow(booking.getItemId());
        UserDto user = getUserFromOrThrow(booking.getBookerId());

        validator.validateChangeBookingStatus(booking, item, userId);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Изменение статуса бронирования с id {} успешно", bookingId);
        return bookingMapper.responseDto(booking, user, item);
    }

    @Override
    public BookingResponseDto getBookingById(long bookingId, long userId) {
        log.info("Получение бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        ItemDto item = getItemFromOrThrow(booking.getItemId());
        UserDto user = getUserFromOrThrow(booking.getBookerId());

        if (booking.getBookerId() != userId && item.ownerId() != userId) {
            throw new NotFoundException("Пользователь не является создателем вещи или создателем бронирования");
        }
        return bookingMapper.responseDto(booking, user, item);
    }

    @Override
    public List<BookingResponseDto> getAllBookingByUser(long userId, String stateStr) {
        log.info("Получение бронирований для пользователя {}", userId);

        getUserFromOrThrow(userId);

        BookingState state = getState(stateStr);
        OffsetDateTime now = OffsetDateTime.now(clock);

        List<Booking> bookings = searchRegistry.search(state, userId, BookingUserRole.BOOKER, now);

        return createResponseDto(bookings);
    }

    @Override
    public List<BookingResponseDto> getAllBookingByOwner(long ownerId, String stateStr) {
        log.info("Получение бронирований для предметов текущего пользователя {}", ownerId);

        getUserFromOrThrow(ownerId);

        BookingState state = getState(stateStr);
        OffsetDateTime now = OffsetDateTime.now(clock);

        List<Booking> bookings = searchRegistry.search(state, ownerId, BookingUserRole.OWNER, now);

        return createResponseDto(bookings);
    }

    @Override
    public Map<Long, List<BookingShortDto>> getBookingForItems(List<Long> itemIds) {
        List<BookingShortDto> bookings = bookingRepository.getAllBookingForItems(itemIds);

        return bookings.stream()
                .collect(Collectors.groupingBy(BookingShortDto::itemId));
    }

    @Override
    public Optional<BookingShortDto> getBookingForComment(long userId, long itemId, OffsetDateTime currentTime) {
        return bookingRepository.getBookingForComment(userId, itemId, currentTime);
    }

    private ItemDto getItemFromOrThrow(long itemId) {
        try {
            return itemClient.getItemById(itemId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Предмет не найден");
        }
    }

    private UserDto getUserFromOrThrow(long userId) {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    private List<BookingResponseDto> createResponseDto(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) return List.of();

        List<Long> userIds = bookings.stream()
                .map(Booking::getBookerId)
                .distinct()
                .toList();

        List<Long> itemIds = bookings.stream()
                .map(Booking::getItemId)
                .distinct()
                .toList();

        List<ItemDto> items = itemClient.getItemsByIds(itemIds);
        List<UserDto> users = userClient.getUsersByIds(userIds);

        final Map<Long, UserDto> usersMap = users.stream()
                .collect(Collectors.toMap(UserDto::id, u -> u));
        final Map<Long, ItemDto> itemsMap = items.stream()
                .collect(Collectors.toMap(ItemDto::id, i -> i));

        return bookings.stream()
                .map(b -> {
                    UserDto userDto = usersMap.get(b.getBookerId());
                    ItemDto itemDto = itemsMap.get(b.getItemId());
                    return bookingMapper.responseDto(b,userDto, itemDto);
                })
                .sorted(Comparator.comparing(BookingResponseDto::start))
                .toList();
    }

    private BookingState getState(String stateStr) {
        try {
            return BookingState.valueOf(stateStr);
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка получения состояния, было передано {}", stateStr);
            throw new ValidatedException("Unknown state: " + stateStr);
        }
    }
}

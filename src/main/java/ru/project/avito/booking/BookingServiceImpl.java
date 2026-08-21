package ru.project.avito.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.project.avito.booking.dto.BookingCreateDto;
import ru.project.avito.booking.dto.mapper.BookingMapper;
import ru.project.avito.booking.dto.BookingResponseDto;
import ru.project.avito.booking.model.Booking;
import ru.project.avito.exceptions.ForbiddenContentException;
import ru.project.avito.exceptions.NotFoundException;
import ru.project.avito.exceptions.ValidatedException;
import ru.project.avito.item.ItemRepository;
import ru.project.avito.item.model.Item;
import ru.project.avito.user.UserRepository;
import ru.project.avito.user.model.User;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final Clock clock;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingCreateDto createDto, long userId) {
        log.info("Создание бронирования для пользоватле с id {} и предмета с id {}", userId, createDto.itemId());
        if (createDto.start().isAfter(createDto.end())) {
            throw new ValidatedException("Начало аренды не должно быть после конца аренды");
        }
        Item item = itemRepository.findById(createDto.itemId())
                .orElseThrow(() -> new NotFoundException("Предмет с id " + createDto.itemId() + " не найден"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        if (!item.getAvailable()) {
            log.warn("Вещь с id {} недоступна для бронирование", item.getId());
            throw new ValidatedException("Вещь сейчас недоступна для бронирования");
        }

        if (item.getOwner().getId() == userId) {
            log.warn("Вещь с id {} недоступна для бронирование", item.getId());
            throw new NotFoundException("Владелец не может забронировать свою же вещь");
        }

        Booking booking = bookingMapper.toBooking(createDto, item, user);
        log.info("Бронирование создано для пользоватле с id {} и предмета с id {}", userId, item.getId());
        return bookingMapper.responseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(long bookingId, boolean approved, long userId) {
        log.info("Изменение статуса бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + "не найдено"));

        if (booking.getItem().getOwner().getId() != userId) {
            throw new ForbiddenContentException("Менять статус может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidatedException("Статус бронирования уже изменен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Изменение статуса бронирования с id {} успешно", bookingId);
        return bookingMapper.responseDto(booking);
    }

    @Override
    public BookingResponseDto getBookingById(long bookingId, long userId) {
        log.info("Получение бронирования с id {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + "не найдено"));

        if (booking.getBooker().getId() == userId || booking.getItem().getOwner().getId() == userId) {
            return bookingMapper.responseDto(booking);
        }
        throw new NotFoundException("Пользователь не является создателем вещи или создателем бронирования");
    }

    @Override
    public List<BookingResponseDto> getAllBookingByUser(long userId, String stateStr) {
        log.info("Получение бронирований для пользователя {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        BookingState state = getState(stateStr);
        OffsetDateTime dateTime = OffsetDateTime.now();

        switch (state) {
            case ALL -> {
                return bookingRepository.findByBookerIdOrderByStartDesc(userId).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case PAST -> {
                return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, dateTime).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case CURRENT -> {
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, dateTime, dateTime).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case FUTURE -> {
                return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, dateTime).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case WAITING, REJECTED -> {
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.valueOf(state.name())).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            default -> throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    @Override
    public List<BookingResponseDto> getAllBookingByOwner(long ownerId, String stateStr) {
        log.info("Получение бронирований для предметов текущего пользователя {}", ownerId);
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("Пользователь с id " + ownerId + " не найден");
        }

        BookingState state = getState(stateStr);
        OffsetDateTime dateTime = OffsetDateTime.now(clock);

        switch (state) {
            case ALL -> {
                return bookingRepository.findByOwnerIdOrderByStartDesc(ownerId).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case PAST -> {
                return bookingRepository.findByOwnerIdAndEndBeforeOrderByStartDesc(ownerId, dateTime).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case CURRENT -> {
                return bookingRepository.findByOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, dateTime, dateTime).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case FUTURE -> {
                return bookingRepository.findByOwnerIdAndStartAfterOrderByStartDesc(ownerId, dateTime).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            case WAITING, REJECTED -> {
                return bookingRepository.findByOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.valueOf(state.name())).stream()
                        .map(bookingMapper::responseDto)
                        .toList();
            }
            default -> throw new IllegalStateException("Unexpected value: " + state);
        }

    }

    private BookingState getState(String stateStr) {
        try {
            return BookingState.valueOf(stateStr);
        } catch (IllegalStateException e) {
            log.warn("Ошибка получения состояния, было передано {}", stateStr);
            throw new ValidatedException("Unknown state: " + stateStr);
        }
    }
}

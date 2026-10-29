package ru.project.item.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.project.commonlib.exceptions.ExternalServiceException;
import ru.project.commonlib.exceptions.NotFoundException;
import ru.project.commonlib.http.booking.BookingShortDto;
import ru.project.commonlib.http.user.UserDto;
import ru.project.item.dao.CommentRepository;
import ru.project.item.dao.ItemRepository;
import ru.project.item.dto.*;
import ru.project.item.dto.mapper.CommentMapper;
import ru.project.item.dto.mapper.ItemMapper;
import ru.project.item.feign.BookingClient;
import ru.project.item.feign.UserForItemClient;
import ru.project.item.model.Comment;
import ru.project.item.model.Item;
import ru.project.item.service.update.ItemFieldUpdate;
import ru.project.item.service.validate.ItemValidation;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final KafkaProducer producer;
    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final UserForItemClient userForItemClient;
    private final BookingClient bookingClient;
    private final Clock clock;
    private final ItemValidation itemValidation;
    private final List<ItemFieldUpdate> fieldUpdates;

    @Override
    public List<ItemDto> searchItems(String text, String city, Long userId) {
        log.info("Поиск предмета по ключевому слову: {}", text);
        if (text == null || text.isBlank()) return List.of();
        String query = text.toLowerCase();
        List<ItemDto> items = itemRepository.findAllBySearching(query);

        producer.sendEvent(query, userId, city, items.size());

        if (items.isEmpty()) {
            return List.of();
        }
        return items;
    }

    @Override
    public List<ItemWithCommentsDto> getAllItems() {
        log.info("Получение списка всех предметов");
        return itemRepository.findAll().stream()
                .map(item -> itemMapper.toItemWithComments(item, item.getComments(), Map.of()))
                .toList();
    }

    @Override
    public List<ItemWithBookingDto> getAllItemsFromUser(long userId) {
        log.info("Получение всех предметов пользователя с id {}", userId);

        getUserOrThrow(userId);

        OffsetDateTime now = OffsetDateTime.now(clock);
        List<Item> items = itemRepository.findAllItemsWithComments(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<Long> ownersIdsComment = items.stream()
                .flatMap(item -> item.getComments().stream().map(Comment::getAuthorId))
                .distinct()
                .toList();

        List<UserDto> users = getUsersOrThrow(ownersIdsComment);

        Map<Long, String> usersName = ownersIdsComment.isEmpty() ? Map.of()
                : users.stream().collect(Collectors.toMap(UserDto::id, UserDto::name));

        Map<Long, List<BookingShortDto>> bookingsByItem = bookingClient.getAllBookingForItems(itemIds);

        return items.stream()
                .map(item -> {
                            List<BookingShortDto> bookings = bookingsByItem.getOrDefault(item.getId(), List.of());

                            BookingShortDto prev = findPrevBooking(bookings, now);

                            BookingShortDto next = findNextBooking(bookings, now);

                            return itemMapper.toItemWithBookingDto(item, item.getComments(), prev, next, usersName);
                        }
                )
                .sorted(Comparator.comparing(ItemWithBookingDto::id))
                .toList();
    }

    @Override
    public ItemWithBookingDto getItemById(long itemId, long userId, String city) {
        log.info("Получение предмета по id {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        List<Long> ownerIdsComment = item.getComments().stream()
                .map(Comment::getAuthorId)
                .toList();

        List<UserDto> users = getUsersOrThrow(ownerIdsComment);

        Map<Long, String> userNames = ownerIdsComment.isEmpty() ? Map.of()
                : users.stream().collect(Collectors.toMap(UserDto::id, UserDto::name));

        OffsetDateTime now = OffsetDateTime.now(clock);

        producer.sendEvent(itemId, userId, city, now.toInstant());

        if (item.getOwnerId() != userId) {
            return itemMapper.toItemWithBookingDto(item, item.getComments(), null, null, userNames);
        }

        Map<Long, List<BookingShortDto>> bookingsByItem = bookingClient.getAllBookingForItems(List.of(itemId));

        List<BookingShortDto> bookings = bookingsByItem.getOrDefault(itemId, List.of());

        BookingShortDto prev = findPrevBooking(bookings, now);

        BookingShortDto next = findNextBooking(bookings, now);

        return itemMapper.toItemWithBookingDto(item, item.getComments(), prev, next, userNames);
    }

    @Override
    @Transactional
    public ItemDto createItem(ItemCreatDto itemCreat, long userId) {
        log.info("Создание предмета от пользователя с id {}", userId);
        itemValidation.initializeItem(itemCreat);

        getUserOrThrow(userId);
        Item item = itemRepository.save(itemMapper.toItem(itemCreat, userId));
        log.info("Предмет с id {} успешно создан", item.getId());
        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public void deleteItemById(long itemId) {
        log.info("Удаление предмета с id {}", itemId);
        itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
        itemRepository.deleteById(itemId);
        log.info("Предмет с id {} удален", itemId);
    }

    @Override
    @Transactional
    public ItemDto updateItem(long itemId, ItemDto itemDto, long userId) {
        log.info("Обновление предмета с id {} и id пользователя {}", itemId, userId);
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));

        if (oldItem.getOwnerId() != userId) {
            log.warn("Пользователь с id {} пытался обновить чужой предмет с id {}", userId, itemId);
            throw new NotFoundException("Предмет с id " + itemId + " не найден");
        }

        fieldUpdates.stream()
                .filter(i -> i.support(itemDto))
                .forEach(i -> i.updateField(oldItem, itemDto));

        Item item = itemRepository.saveAndFlush(oldItem);

        log.info("Предмет обновлен {}", item);
        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public CommentDto writeAComment(CommentCreateDto newComment, long userId, long itemId) {
        log.info("Добавление комментария предмету {} от пользователя {}", itemId, userId);
        UserDto user = getUserOrThrow(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
        OffsetDateTime now = OffsetDateTime.now(clock);

        itemValidation.verifyBookingExist(userId, itemId, now);

        Comment createdComment = commentMapper.toComment(newComment);
        createdComment.setItem(item);
        createdComment.setAuthorId(userId);
        createdComment.setCreated(now);

        Comment comment = commentRepository.save(createdComment);
        log.info("Комментарий с id {} успешно создан", comment.getId());
        return commentMapper.toCommentDto(comment, Map.of(user.id(), user.name()));
    }

    @Override
    public ItemDtoForBookingService getItemByIdForBooking(long itemId) {
        return itemRepository.findItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
    }

    @Override
    public List<ItemDtoForBookingService> getItemsByIdsForBooking(List<Long> itemIds) {
        return itemRepository.findItemsByIds(itemIds);
    }

    @Override
    public List<Long> getIdsItemsForBooking(long userId) {
        return itemRepository.findItemByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteAllItemsForUser(long userId) {
        log.info("Пакетное удаление всех предметов владельца с id {}", userId);
        itemRepository.deleteAllByOwnerId(userId);
    }

    private BookingShortDto findPrevBooking(List<BookingShortDto> bookings, OffsetDateTime now) {
        return bookings.stream()
                .filter(b -> b.start().isBefore(now) || b.start().isEqual(now))
                .max(Comparator.comparing(BookingShortDto::start))
                .orElse(null);
    }

    private BookingShortDto findNextBooking(List<BookingShortDto> bookings, OffsetDateTime now) {
        return bookings.stream()
                .filter(b -> b.start().isAfter(now))
                .min(Comparator.comparing(BookingShortDto::start))
                .orElse(null);
    }

    private UserDto getUserOrThrow(long userId) {
        try {
            return userForItemClient.getUserById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователь не найден");
        } catch (FeignException e) {
            throw new ExternalServiceException("Сервис недоступен");
        }
    }

    private List<UserDto> getUsersOrThrow(List<Long> userIds) {
        try {
            return userForItemClient.getUsersByUserIds(userIds);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Пользователи не найдены");
        } catch (FeignException e) {
            throw new ExternalServiceException("Сервис недоступен");
        }
    }
}

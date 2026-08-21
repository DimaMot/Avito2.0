package ru.project.avito.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.project.avito.booking.BookingRepository;
import ru.project.avito.booking.dto.BookingShortDto;
import ru.project.avito.exceptions.NotFoundException;
import ru.project.avito.exceptions.ValidatedException;
import ru.project.avito.item.dto.*;
import ru.project.avito.item.dto.mapper.CommentMapper;
import ru.project.avito.item.dto.mapper.ItemMapper;
import ru.project.avito.item.model.Comment;
import ru.project.avito.item.model.Item;
import ru.project.avito.user.UserRepository;
import ru.project.avito.user.model.User;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final Clock clock;

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск предмета по ключевому слову: {}", text);
        if (text == null || text.isBlank()) return List.of();
        String query = text.toLowerCase();
        return itemRepository.findAllBySearching(query).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemWithCommentsDto> getAllItems() {
        log.info("Получение списка всех предметов");
        return itemRepository.findAll().stream()
                .map(item -> itemMapper.toItemWithComments(item, item.getComments()))
                .toList();
    }

    @Override
    public List<ItemWithBookingDto> getAllItemsFromUser(long userId) {
        log.info("Получение всех предметов пользователя с id {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<BookingShortDto> bookings = bookingRepository.findByItemId(itemIds);

        Map<Long, List<BookingShortDto>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(BookingShortDto::itemId));

        return items.stream()
                .map(item -> {
                            List<BookingShortDto> bookingItem = bookingsByItem.getOrDefault(item.getId(), List.of());

                            BookingShortDto prev = bookingItem.stream()
                                    .filter(b -> b.start().isBefore(now))
                                    .max(Comparator.comparing(BookingShortDto::start))
                                    .orElse(null);

                            BookingShortDto next = bookingItem.stream()
                                    .filter(b -> b.start().isAfter(now))
                                    .min(Comparator.comparing(BookingShortDto::start))
                                    .orElse(null);

                            return itemMapper.toItemWithBookingDto(item, item.getComments(), prev, next);
                        }
                )
                .sorted(Comparator.comparing(ItemWithBookingDto::id))
                .toList();
    }

    @Override
    public ItemWithCommentsDto getItemById(long itemId) {
        log.info("Получение предмета по id {}", itemId);
        return itemRepository.findById(itemId)
                .map(i -> itemMapper.toItemWithComments(i, i.getComments()))
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
    }

    @Override
    @Transactional
    public ItemDto creatItem(ItemCreatDto itemCreat, long userId) {
        log.info("Создание предмета от пользователя с id {}", userId);
        if (itemCreat != null) {
            Item item = itemMapper.toItem(itemCreat);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
            item.setOwner(user);
            return itemMapper.toItemDto(itemRepository.save(item));
        }
        throw new ValidatedException("Предмет должен быть инициализирован");
    }

    @Override
    @Transactional
    public void deleteItemById(long itemId) {
        log.info("Удаление предмета с id {}", itemId);
        itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
        itemRepository.deleteById(itemId);
        log.info("Предмета с id {} удален", itemId);
    }

    @Override
    @Transactional
    public ItemDto updateItem(long itemId, ItemDto itemDto, long userId) {
        log.info("Обновление предмета с id {} и id пользователя {}", itemId, userId);
        Item oldItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
        if (oldItem.getOwner().getId() != userId) {
            log.warn("Пользователь с id {} пытался обновить чужой предмет с id {}", userId, itemId);
            throw new NotFoundException("Пользователь с id " + userId + " не является собственником предмета с id " + itemId);
        }

        if (itemDto.available() != null) {
            oldItem.setAvailable(itemDto.available());
        }
        if (itemDto.name() != null) {
            oldItem.setName(itemDto.name());
        }
        if (itemDto.description() != null) {
            oldItem.setDescription(itemDto.description());
        }
        log.info("Предмет обновлен {}", oldItem);
        return itemMapper.toItemDto(oldItem);
    }

    @Override
    @Transactional
    public ItemWithCommentsDto writeAComment(CommentCreateDto comment, long userId, long itemId) {
        log.info("Добавление комментария предмету {} от пользователя {}", itemId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с " + userId + " не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
        OffsetDateTime now = OffsetDateTime.now(clock);

        Optional<BookingShortDto> bookingShortDto = bookingRepository.findByBookerId(userId, itemId, now);

        if (bookingShortDto.isEmpty()) {
            throw new ValidatedException("Пользователь " + userId + " не брал предмет " + itemId + " в аренду или она еще не закончилась");
        }

        Comment createdComment = commentMapper.toComment(comment);
        createdComment.setItem(item);
        createdComment.setAuthor(user);
        createdComment.setCreated(now);
        commentRepository.save(createdComment);
        if (item.getComments() != null) {
            item.getComments().add(createdComment);
        }
        log.info("Комментарий предмету {} от пользователя {} успешно добавлен", itemId, userId);
        return itemMapper.toItemWithComments(item, item.getComments());
    }
}

package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidatedException;
import ru.practicum.shareit.item.dto.ItemCreatDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск предмета по ключевому слову: {}", text);
        if (text == null || text.isBlank()) return List.of();
        String query = text.toLowerCase();
        return itemRepository.search(query).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> getAllItems() {
        log.info("Получение списка всех предметов");
        return itemRepository.getAllItems().stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> getAllItemsFromUser(long userId) {
        log.info("Получение всех предметов пользователя с id {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return itemRepository.getAllItemsFromUser(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto getItemById(long itemId) {
        log.info("Получение предмета по id {}", itemId);
        return itemRepository.getItemById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден"));
    }

    @Override
    public ItemDto creatItem(ItemCreatDto itemCreat, long userId) {
        log.info("Создание предмета от пользователя с id {}", userId);
        if (itemCreat != null) {
            Item item = ItemMapper.toItem(itemCreat);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
            item.setOwner(user);
            return ItemMapper.toItemDto(itemRepository.createItem(item));
        }
        throw new ValidatedException("Предмет должен быть инициализирован");
    }

    @Override
    public void deleteItemById(long itemId) {
        log.info("Удаление предмета с id {}", itemId);
        Item item = itemRepository.deleteItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Предмет с id " + itemId + " не найден");
        }
        log.info("Предмета с id {} удален", itemId);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long userId) {
        log.info("Обновление предмета с id {} и id пользователя {}", itemDto.getId(), userId);
        Item oldItem = itemRepository.getItemById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemDto.getId() + " не найден"));
        if (oldItem.getOwner().getId() != userId) {
            log.warn("Пользователь с id {} пытался обновить чужой предмет с id {}", userId, itemDto.getId());
            throw new NotFoundException("Пользователь с id " + userId + " не является собственником предмета с id " + itemDto.getId());
        }

        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }
        log.info("Предмет обновлен {}", oldItem);
        return ItemMapper.toItemDto(oldItem);
    }
}

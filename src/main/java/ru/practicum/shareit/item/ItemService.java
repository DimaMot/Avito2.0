package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemCreatDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> searchItems(String text);

    List<ItemDto> getAllItems();

    List<ItemDto> getAllItemsFromUser(long userId);

    ItemDto getItemById(long itemId);

    ItemDto creatItem(ItemCreatDto itemCreat, long userId);

    void deleteItemById(long itemId);

    ItemDto updateItem(ItemDto itemDto, long userId);
}

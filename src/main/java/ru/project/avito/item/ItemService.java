package ru.project.avito.item;

import ru.project.avito.item.dto.*;

import java.util.List;

public interface ItemService {
    List<ItemDto> searchItems(String text);

    List<ItemWithCommentsDto> getAllItems();

    List<ItemWithBookingDto> getAllItemsFromUser(long userId);

    ItemWithCommentsDto getItemById(long itemId);

    ItemDto creatItem(ItemCreatDto itemCreat, long userId);

    void deleteItemById(long itemId);

    ItemDto updateItem(long itemId, ItemDto itemDto, long userId);

    ItemWithCommentsDto writeAComment(CommentCreateDto comment, long userId, long itemId);
}

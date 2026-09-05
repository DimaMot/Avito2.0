package ru.project.item.service;

import ru.project.item.dto.*;

import java.util.List;

public interface ItemService {
    List<ItemDto> searchItems(String text);

    List<ItemWithCommentsDto> getAllItems();

    List<ItemWithBookingDto> getAllItemsFromUser(long userId);

    ItemWithBookingDto getItemById(long itemId, long userId);

    ItemDto creatItem(ItemCreatDto itemCreat, long userId);

    void deleteItemById(long itemId);

    ItemDto updateItem(long itemId, ItemDto itemDto, long userId);

    CommentDto writeAComment(CommentCreateDto comment, long userId, long itemId);

    ItemDtoForBookingService getItemByIdForBooking(long itemId);

    List<ItemDtoForBookingService> getItemsByIdsForBooking(List<Long> itemIds);

    List<Long> getIdsItemsForBooking(long userId);

    void deleteAllItemsForUser(long userId);
}

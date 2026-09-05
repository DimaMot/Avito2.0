package ru.project.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.project.item.dto.*;
import ru.project.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private static final String USER_HEADER = "X-Avito-User-Id";

    @GetMapping("/{itemId}")
    public ItemWithBookingDto getItem(@Positive @PathVariable long itemId,
                                      @RequestHeader(USER_HEADER) long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@Valid @RequestBody ItemCreatDto item,
                              @RequestHeader(USER_HEADER) long userId) {
        return itemService.creatItem(item, userId);
    }

    @GetMapping
    public List<ItemWithBookingDto> getItemFromUserId(@NotNull @RequestHeader(USER_HEADER) long userId) {
        return itemService.getAllItemsFromUser(userId);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@Positive @PathVariable long itemId) {
        itemService.deleteItemById(itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto updateItem,
                              @PathVariable long itemId,
                              @RequestHeader(USER_HEADER) long userId) {
        return itemService.updateItem(itemId, updateItem, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto writeAComment(@RequestHeader(USER_HEADER) long userId,
                                             @Valid @RequestBody CommentCreateDto comment,
                                             @PathVariable long itemId) {
        return itemService.writeAComment(comment, userId, itemId);
    }

    @GetMapping("/{itemId}/forBooking")
    public ItemDtoForBookingService getItemById(@PathVariable("itemId") long itemId) {
        return itemService.getItemByIdForBooking(itemId);
    }

    @GetMapping("/forBooking")
    public List<ItemDtoForBookingService> getItemsByIds(@RequestParam("itemIds") List<Long> itemsIds) {
        return itemService.getItemsByIdsForBooking(itemsIds);
    }

    @GetMapping("/owner/{userId}/forBooking")
    public List<Long> getItemsIdsByUserId(@PathVariable("userId") long userId) {
        return itemService.getIdsItemsForBooking(userId);
    }

    @DeleteMapping("/owner/{userId}/delete-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllItemsFromUser(@PathVariable("userId") long userId) {
        itemService.deleteAllItemsForUser(userId);
    }
}

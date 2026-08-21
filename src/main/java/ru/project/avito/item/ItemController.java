package ru.project.avito.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.project.avito.item.dto.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @GetMapping("/{itemId}")
    public ItemWithCommentsDto getItem(@Positive @PathVariable long itemId) {
        return itemService.getItemById(itemId);
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
    public ItemWithCommentsDto writeAComment(@RequestHeader(USER_HEADER) long userId,
                                             @Valid @RequestBody CommentCreateDto comment,
                                             @PathVariable long itemId) {
        return itemService.writeAComment(comment, userId, itemId);
    }
}

package ru.project.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.project.booking.dto.ItemDto;

import java.util.List;

@FeignClient(name = "item-server", url = "http://item-server:8082")
public interface ItemForBookingClient {
    @GetMapping("/items/{itemId}/forBooking")
    ItemDto getItemById(@PathVariable("itemId") long itemId);

    @GetMapping("/items/forBooking")
    List<ItemDto> getItemsByIds(@RequestParam("itemIds") List<Long> itemsIds);

    @GetMapping("/items/owner/{userId}/forBooking")
    List<Long> getItemsIdsByUserId(@PathVariable("userId") long userId);
}

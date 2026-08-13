package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;

@UtilityClass
public class ItemMapper {
    public static Item toItem(ItemCreatDto itemCreatDto) {
        Item item = new Item();
        item.setName(itemCreatDto.getName());
        item.setAvailable(itemCreatDto.getAvailable());
        item.setDescription(itemCreatDto.getDescription());
        return item;
    }

    public static ItemDto toItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        return dto;
    }
}

package ru.project.avito.item.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.project.avito.booking.dto.BookingShortDto;
import ru.project.avito.item.dto.ItemCreatDto;
import ru.project.avito.item.dto.ItemDto;
import ru.project.avito.item.dto.ItemWithBookingDto;
import ru.project.avito.item.dto.ItemWithCommentsDto;
import ru.project.avito.item.model.Comment;
import ru.project.avito.item.model.Item;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CommentMapper.class})
public interface ItemMapper {
    Item toItem(ItemCreatDto create);

    ItemDto toItemDto(Item item);

    @Mapping(source = "item.id", target = "id")
    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.description", target = "description")
    @Mapping(source = "item.available", target = "available")
    @Mapping(source = "comments", target = "comments")
    ItemWithCommentsDto toItemWithComments(Item item, List<Comment> comments);

    @Mapping(source = "item.id", target = "id")
    @Mapping(source = "item.name", target = "name")
    @Mapping(source = "item.description", target = "description")
    @Mapping(source = "item.available", target = "available")
    @Mapping(source = "prev", target = "prevBooking")
    @Mapping(source = "next", target = "nextBooking")
    ItemWithBookingDto toItemWithBookingDto(Item item, List<Comment> comments, BookingShortDto prev, BookingShortDto next);
}

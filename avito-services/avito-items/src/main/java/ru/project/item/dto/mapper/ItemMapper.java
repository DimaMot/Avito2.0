package ru.project.item.dto.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.project.item.dto.BookingShortDto;
import ru.project.item.dto.ItemCreatDto;
import ru.project.item.dto.ItemDto;
import ru.project.item.dto.ItemWithBookingDto;
import ru.project.item.dto.ItemWithCommentsDto;
import ru.project.item.model.Comment;
import ru.project.item.model.Item;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CommentMapper.class})
public interface ItemMapper {
    @Mapping(source = "userId", target = "ownerId")
    Item toItem(ItemCreatDto create, long userId);

    ItemDto toItemDto(Item item);

    @Mapping(source = "comments", target = "comments")
    ItemWithCommentsDto toItemWithComments(Item item, List<Comment> comments, @Context Map<Long, String> userNames);

    @Mapping(source = "item.id", target = "id")
    @Mapping(source = "prev", target = "prevBooking")
    @Mapping(source = "next", target = "nextBooking")
    ItemWithBookingDto toItemWithBookingDto(Item item,
                                            List<Comment> comments,
                                            BookingShortDto prev,
                                            BookingShortDto next,
                                            @Context Map<Long, String> userNames);
}

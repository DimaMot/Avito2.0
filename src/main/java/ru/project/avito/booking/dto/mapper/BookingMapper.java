package ru.project.avito.booking.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.project.avito.booking.dto.BookingCreateDto;
import ru.project.avito.booking.dto.BookingResponseDto;
import ru.project.avito.booking.model.Booking;
import ru.project.avito.item.dto.mapper.ItemMapper;
import ru.project.avito.item.model.Item;
import ru.project.avito.user.dto.mapper.UserMapper;
import ru.project.avito.user.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {
    @Mapping(source = "create.start", target = "start")
    @Mapping(source = "create.end", target = "end")
    @Mapping(source = "item", target = "item")
    @Mapping(source = "booker", target = "booker")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "WAITING")
    Booking toBooking(BookingCreateDto create, Item item, User booker);

    BookingResponseDto responseDto(Booking booking);
}

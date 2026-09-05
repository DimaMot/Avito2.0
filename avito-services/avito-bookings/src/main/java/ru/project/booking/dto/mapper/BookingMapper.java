package ru.project.booking.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.project.booking.dto.*;
import ru.project.booking.model.Booking;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "WAITING")
    Booking toBooking(BookingCreateDto create, Long itemId, Long bookerId);

    @Mapping(source = "booking.id", target = "id")
    @Mapping(source = "user", target = "booker")
    @Mapping(source = "item", target = "item")
    BookingResponseDto responseDto(Booking booking, UserDto user, ItemDto item);
}

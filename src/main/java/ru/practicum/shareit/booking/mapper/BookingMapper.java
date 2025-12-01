package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                new BookingDto.ItemInfo(
                        booking.getItem().getId(),
                        booking.getItem().getName()
                ),
                new BookingDto.UserInfo(
                        booking.getBooker().getId(),
                        booking.getBooker().getName()
                ),
                booking.getStatus()
        );
    }
}
package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                new BookingItemDto(
                        booking.getItem().getId(),
                        booking.getItem().getName()
                ),
                new BookingUserDto(
                        booking.getBooker().getId(),
                        booking.getBooker().getName()
                ),
                booking.getStatus()
        );
    }
}
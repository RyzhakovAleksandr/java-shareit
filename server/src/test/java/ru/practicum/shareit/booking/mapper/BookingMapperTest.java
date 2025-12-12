package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toBookingDto() {
        User owner = new User(1L, "Owner", "owner@example.com");
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Drill", "Powerful drill", true, owner, null);

        Booking booking = new Booking(
                1L,
                LocalDateTime.of(2024, 1, 10, 10, 0),
                LocalDateTime.of(2024, 1, 15, 10, 0),
                item,
                booker,
                BookingStatus.APPROVED
        );

        BookingDto dto = BookingMapper.toBookingDto(booking);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 10, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getItem().getId()).isEqualTo(1L);
        assertThat(dto.getItem().getName()).isEqualTo("Drill");
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
        assertThat(dto.getBooker().getName()).isEqualTo("Booker");
    }
}
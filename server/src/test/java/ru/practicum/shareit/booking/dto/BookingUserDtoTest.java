package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BookingUserDtoTest {

    @Test
    void shouldHaveWorkingAllArgsConstructor() {
        BookingUserDto dto = new BookingUserDto(1L, "John");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John");
    }
}
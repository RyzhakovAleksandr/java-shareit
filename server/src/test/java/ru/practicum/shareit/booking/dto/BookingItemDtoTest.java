package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BookingItemDtoTest {

    @Test
    void shouldHaveWorkingAllArgsConstructor() {
        BookingItemDto dto = new BookingItemDto(1L, "Drill");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
    }
}
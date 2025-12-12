package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestDtoTest {

    @Test
    void shouldHaveWorkingGettersAndSetters() {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need item");
        dto.setRequestorId(10L);
        dto.setCreated(LocalDateTime.now());

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need item");
        assertThat(dto.getRequestorId()).isEqualTo(10L);
    }
}
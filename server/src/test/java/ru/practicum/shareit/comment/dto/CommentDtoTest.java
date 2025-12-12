package ru.practicum.shareit.comment.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class CommentDtoTest {

    @Test
    void shouldHaveWorkingGettersAndSetters() {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("Great!");
        dto.setAuthorName("John");
        dto.setCreated(LocalDateTime.now());

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Great!");
        assertThat(dto.getAuthorName()).isEqualTo("John");
    }
}
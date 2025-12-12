package ru.practicum.shareit.comment.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CommentCreateDtoTest {

    @Test
    void shouldHaveWorkingGettersAndSetters() {
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("Great item!");

        assertThat(dto.getText()).isEqualTo("Great item!");
    }

    @Test
    void shouldHaveWorkingAllArgsConstructor() {
        CommentCreateDto dto = new CommentCreateDto("Great item!");

        assertThat(dto.getText()).isEqualTo("Great item!");
    }
}
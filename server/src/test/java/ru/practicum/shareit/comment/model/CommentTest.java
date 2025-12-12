package ru.practicum.shareit.comment.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    void shouldHaveWorkingGettersAndSetters() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setCreated(LocalDateTime.now());

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Great item!");
        assertThat(comment.getCreated()).isNotNull();
    }

    @Test
    void shouldHaveWorkingAllArgsConstructor() {
        LocalDateTime created = LocalDateTime.now();
        Item item = new Item();
        User author = new User();

        Comment comment = new Comment(1L, "Great item!", item, author, created);

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Great item!");
        assertThat(comment.getCreated()).isEqualTo(created);
    }
}
package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Test
    void shouldHaveWorkingGettersAndSetters() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.isAvailable()).isTrue();
    }

    @Test
    void shouldHaveWorkingAllArgsConstructor() {
        User owner = new User();

        Item item = new Item(1L, "Drill", "Powerful drill", true, owner, null);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.isAvailable()).isTrue();
    }
}
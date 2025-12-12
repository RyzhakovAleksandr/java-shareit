package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toItemDto() {
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Drill", "Powerful drill", true, owner, null);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Powerful drill");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void toItem() {
        ItemDto dto = new ItemDto(1L, "Drill", "Powerful drill", true, null);

        Item item = ItemMapper.toItem(dto);

        assertThat(item).isNotNull();
        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.getDescription()).isEqualTo("Powerful drill");
        assertThat(item.isAvailable()).isTrue();
        assertThat(item.getRequest()).isNull();
    }
}
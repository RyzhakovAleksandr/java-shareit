package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @Test
    void createItem() {
        ItemDto request = new ItemDto();
        ItemDto expected = new ItemDto();
        when(itemService.createItem(request, 1L)).thenReturn(expected);

        ItemDto result = itemController.createItem(request, 1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void searchItems() {
        List<ItemDto> expected = Collections.singletonList(new ItemDto());
        when(itemService.searchItems("drill")).thenReturn(expected);

        List<ItemDto> result = itemController.searchItems("drill");

        assertThat(result).isEqualTo(expected);
    }
}
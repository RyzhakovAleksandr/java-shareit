package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDtoWithBookings getItemById(Long id, Long userId);

    List<ItemDtoWithBookings> getItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    void deleteItem(Long id, Long ownerId);

    CommentDto addComment(Long itemId, CommentDto commentDto, Long authorId);
}

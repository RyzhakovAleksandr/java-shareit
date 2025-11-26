package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", ownerId)));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new IllegalArgumentException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(String.format("Вещь с ID %d не найдена", itemId)));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Вещь с ID %d не найдена", id)));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", ownerId)));

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long id, Long ownerId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Вещь с ID %d не найдена", id)));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Только владелец может удалить вещь");
        }

        itemRepository.deleteById(id);
    }
}

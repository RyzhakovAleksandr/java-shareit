package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long requestorId);

    ItemRequestDto getItemRequestById(Long requestId, Long userId);

    List<ItemRequestDto> getItemRequestsByUser(Long requestorId);

    List<ItemRequestDto> getAllItemRequests(Long userId);

    void deleteItemRequest(Long requestId, Long requestorId);
}

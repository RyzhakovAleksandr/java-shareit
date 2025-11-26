package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto createItemRequest(ItemRequestDto itemRequestDto, Long requestorId) {
        var requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", requestorId)));

        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание запроса не может быть пустым");
        }
        if (itemRequestDto.getCreated() == null) {
            itemRequestDto.setCreated(LocalDateTime.now());
        }
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requestor);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public ItemRequestDto getItemRequestById(Long requestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", userId)));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Запрос с ID %d не найден", requestId)));

        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getItemRequestsByUser(Long requestorId) {
        userRepository.findById(requestorId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", requestorId)));

        return itemRequestRepository.findByRequestorId(requestorId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", userId)));

        return itemRequestRepository.findAll().stream()
                .filter(request -> !request.getRequestor().getId().equals(userId))
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItemRequest(Long requestId, Long requestorId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Запрос с ID %d не найден", requestId)));

        if (!itemRequest.getRequestor().getId().equals(requestId)) {
            throw new IllegalArgumentException("Можно удалять только свои запросы");
        }

        itemRequestRepository.deleteById(requestId);
    }
}

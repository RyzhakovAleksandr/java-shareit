package ru.practicum.shareit.request.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRequestRepository implements ItemRequestRepository {
    private final Map<Long, ItemRequest> itemRequests = new HashMap<>();
    private long idCounter = 1;

    @Override
    public ItemRequest save(ItemRequest itemRequest) {
        if (itemRequest.getId() == null) {
            itemRequest.setId(idCounter++);
        }
        itemRequests.put(itemRequest.getId(), itemRequest);
        return itemRequest;
    }

    @Override
    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(itemRequests.get(id));
    }

    @Override
    public List<ItemRequest> findAll() {
        return new ArrayList<>(itemRequests.values());
    }

    @Override
    public List<ItemRequest> findByRequestorId(Long requestorId) {
        return itemRequests.values().stream()
                .filter(request -> request.getRequestor().getId().equals(requestorId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        itemRequests.remove(id);
    }
}

package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryItemRepository implements ItemRepository{
    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Item save(Item item) {
        if(item.getId() == null) {
            item.setId(idCounter++);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null
                        && ownerId.equals(item.getOwner().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::isAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerText)
                        || item.getDescription().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }
}

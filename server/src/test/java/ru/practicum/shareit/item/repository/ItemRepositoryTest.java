package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);
    }

    @Test
    void findByOwnerIdWithRequestWhenOwnerHasItems() {
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setOwner(owner);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        List<Item> items = itemRepository.findByOwnerIdWithRequest(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getName)
                .containsExactlyInAnyOrder("Item 1", "Item 2");
    }

    @Test
    void findByOwnerIdWithRequestWhenOwnerHasNoItems() {
        List<Item> items = itemRepository.findByOwnerIdWithRequest(owner.getId());

        assertThat(items).isEmpty();
    }

    @Test
    void searchWithRequestWhenTextMatches() {
        Item item1 = new Item();
        item1.setName("Power Drill");
        item1.setDescription("Professional electric drill");
        item1.setAvailable(true);
        item1.setOwner(owner);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Hammer");
        item2.setDescription("Simple hammer");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        Item unavailableItem = new Item();
        unavailableItem.setName("Broken Drill");
        unavailableItem.setDescription("Broken electric drill");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        List<Item> result = itemRepository.searchWithRequest("drill");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Power Drill");
    }

    @Test
    void searchWithRequestWhenNoMatches() {
        Item item = new Item();
        item.setName("Hammer");
        item.setDescription("Simple hammer");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        List<Item> result = itemRepository.searchWithRequest("drill");

        assertThat(result).isEmpty();
    }

    @Test
    void searchWithRequestWhenTextEmpty() {
        List<Item> result = itemRepository.searchWithRequest("");

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByRequestIdWhenItemsWithRequestExist() {
        List<Item> result = itemRepository.findAllByRequestId(999L);

        assertThat(result).isEmpty();
    }
}
package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void shouldSaveItemRequest() {
        User requester = createUser("Requester", "requester@example.com");
        entityManager.persist(requester);
        entityManager.flush();

        ItemRequest itemRequest = ItemRequest.builder()
                .description("Need a drill")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        ItemRequest saved = itemRequestRepository.save(itemRequest);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Need a drill");
        assertThat(saved.getRequester()).isEqualTo(requester);
    }

    @Test
    void shouldFindAllByRequesterIdOrderByCreatedDesc() {
        User requester1 = createUser("User1", "user1@example.com");
        User requester2 = createUser("User2", "user2@example.com");
        entityManager.persist(requester1);
        entityManager.persist(requester2);
        entityManager.flush();

        ItemRequest request1 = createItemRequest("Request 1", requester1, LocalDateTime.now().minusDays(1));
        ItemRequest request2 = createItemRequest("Request 2", requester1, LocalDateTime.now());
        ItemRequest request3 = createItemRequest("Request 3", requester2, LocalDateTime.now());

        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.persist(request3);
        entityManager.flush();

        List<ItemRequest> found = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requester1.getId());

        assertThat(found).hasSize(2);
        assertThat(found).extracting(ItemRequest::getDescription)
                .containsExactly("Request 2", "Request 1"); // Sorted by created DESC
    }

    @Test
    void shouldFindAllByRequesterIdNotWithPagination() {

        User requester1 = createUser("User1", "user1@example.com");
        User requester2 = createUser("User2", "user2@example.com");
        User requester3 = createUser("User3", "user3@example.com");
        entityManager.persist(requester1);
        entityManager.persist(requester2);
        entityManager.persist(requester3);
        entityManager.flush();

        for (int i = 1; i <= 5; i++) {
            ItemRequest request = createItemRequest(
                    "Request " + i,
                    i % 2 == 0 ? requester2 : requester3,
                    LocalDateTime.now().minusHours(i)
            );
            entityManager.persist(request);
        }
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 3, Sort.by("created").descending());

        List<ItemRequest> found = itemRequestRepository.findAllByRequesterIdNot(requester1.getId(), pageable);

        assertThat(found).hasSize(3);
        assertThat(found).noneMatch(request -> request.getRequester().getId().equals(requester1.getId()));
    }

    @Test
    void shouldFindByRequestorIdWithRequestor() {
        User requester = createUser("Requester", "requester@example.com");
        entityManager.persist(requester);
        entityManager.flush();

        ItemRequest request1 = createItemRequest("Request 1", requester, LocalDateTime.now());
        ItemRequest request2 = createItemRequest("Request 2", requester, LocalDateTime.now().minusHours(1));

        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.flush();
        entityManager.clear();

        List<ItemRequest> found = itemRequestRepository.findByRequestorIdWithRequestor(requester.getId());

        assertThat(found).hasSize(2);
        assertThat(found.getFirst().getRequester()).isNotNull();
        assertThat(found.getFirst().getRequester().getName()).isEqualTo("Requester");
    }

    @Test
    void shouldFindAllWithRequestor() {
        User requester1 = createUser("User1", "user1@example.com");
        User requester2 = createUser("User2", "user2@example.com");
        entityManager.persist(requester1);
        entityManager.persist(requester2);
        entityManager.flush();

        ItemRequest request1 = createItemRequest("Request 1", requester1, LocalDateTime.now());
        ItemRequest request2 = createItemRequest("Request 2", requester2, LocalDateTime.now().minusHours(1));

        entityManager.persist(request1);
        entityManager.persist(request2);
        entityManager.flush();
        entityManager.clear();

        List<ItemRequest> found = itemRequestRepository.findAllWithRequestor();

        assertThat(found).hasSize(2);
        assertThat(found).allMatch(request -> request.getRequester() != null);
        assertThat(found).extracting(request -> request.getRequester().getName())
                .containsExactlyInAnyOrder("User1", "User2");
    }

    @Test
    void shouldReturnEmptyListWhenNoRequestsFound() {
        User requester = createUser("Requester", "requester@example.com");
        entityManager.persist(requester);
        entityManager.flush();

        List<ItemRequest> found = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requester.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteItemRequest() {

        User requester = createUser("Requester", "requester@example.com");
        entityManager.persist(requester);
        entityManager.flush();

        ItemRequest request = createItemRequest("Request to delete", requester, LocalDateTime.now());
        entityManager.persist(request);
        entityManager.flush();

        Long requestId = request.getId();

        itemRequestRepository.deleteById(requestId);
        entityManager.flush();

        ItemRequest deleted = entityManager.find(ItemRequest.class, requestId);
        assertThat(deleted).isNull();
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemRequest createItemRequest(String description, User requester, LocalDateTime created) {
        return ItemRequest.builder()
                .description(description)
                .requester(requester)
                .created(created)
                .build();
    }
}
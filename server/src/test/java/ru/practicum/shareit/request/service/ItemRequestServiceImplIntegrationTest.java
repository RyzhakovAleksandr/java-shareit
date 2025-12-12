package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester;
    private User anotherUser;
    private ItemRequestCreateDto createDto;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setName("Requester");
        requester.setEmail("requester@example.com");
        requester = userRepository.save(requester);

        anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");
        anotherUser = userRepository.save(anotherUser);

        createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need a drill for home repairs");
    }

    @Test
    void createRequestWenValidData() {

        ItemRequestResponseDto createdRequest = itemRequestService.createRequest(requester.getId(), createDto);

        assertThat(createdRequest).isNotNull();
        assertThat(createdRequest.getId()).isNotNull();
        assertThat(createdRequest.getDescription()).isEqualTo("Need a drill for home repairs");
        assertThat(createdRequest.getCreated()).isNotNull();
        assertThat(createdRequest.getItems()).isEmpty();

        ItemRequest savedRequest = itemRequestRepository.findById(createdRequest.getId()).orElseThrow();
        assertThat(savedRequest.getDescription()).isEqualTo("Need a drill for home repairs");
        assertThat(savedRequest.getRequester().getId()).isEqualTo(requester.getId());
    }

    @Test
    void createRequestWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.createRequest(999L, createDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");
    }

    @Test
    void createRequestWhenDescriptionIsBlank() {
        createDto.setDescription("   ");

        assertThatThrownBy(() -> itemRequestService.createRequest(requester.getId(), createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Описание запроса не может быть пустым");
    }

    @Test
    void createRequestWhenDescriptionIsNull() {
        createDto.setDescription(null);

        assertThatThrownBy(() -> itemRequestService.createRequest(requester.getId(), createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Описание запроса не может быть пустым");
    }

    @Test
    void getUserRequestsWhenUserHasRequests() {
        ItemRequest request1 = new ItemRequest();
        request1.setDescription("Need item 1");
        request1.setRequester(requester);
        request1.setCreated(LocalDateTime.now().minusDays(2));
        itemRequestRepository.save(request1);

        ItemRequest request2 = new ItemRequest();
        request2.setDescription("Need item 2");
        request2.setRequester(requester);
        request2.setCreated(LocalDateTime.now().minusDays(1));
        itemRequestRepository.save(request2);

        List<ItemRequestResponseDto> requests = itemRequestService.getUserRequests(requester.getId());

        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(ItemRequestResponseDto::getDescription)
                .containsExactly("Need item 2", "Need item 1"); // Сортировка по дате создания DESC
    }

    @Test
    void getUserRequestsWhenUserHasNoRequests() {

        List<ItemRequestResponseDto> requests = itemRequestService.getUserRequests(requester.getId());

        assertThat(requests).isEmpty();
    }

    @Test
    void getUserRequestsWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getUserRequests(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");
    }

    @Test
    void getAllRequestsWhenOtherUsersHaveRequests() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Need something");
        request.setRequester(anotherUser);
        request.setCreated(LocalDateTime.now());
        itemRequestRepository.save(request);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequests(requester.getId(), pageRequest);

        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getDescription()).isEqualTo("Need something");
        assertThat(requests.getFirst().getItems()).isEmpty();
    }

    @Test
    void getAllRequestsWhenNoOtherRequests() {
        ItemRequest ownRequest = new ItemRequest();
        ownRequest.setDescription("My own request");
        ownRequest.setRequester(requester);
        ownRequest.setCreated(LocalDateTime.now());
        itemRequestRepository.save(ownRequest);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequests(requester.getId(), pageRequest);

        assertThat(requests).isEmpty();
    }

    @Test
    void getAllRequestsWhenRequestsWithItems() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Need a drill");
        request.setRequester(anotherUser);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);

        Item item = new Item();
        item.setName("Power Drill");
        item.setDescription("Professional power drill");
        item.setAvailable(true);
        item.setOwner(requester);
        item.setRequest(request);
        itemRepository.save(item);

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequests(requester.getId(), pageRequest);

        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getItems()).hasSize(1);
        assertThat(requests.getFirst().getItems().getFirst().getName()).isEqualTo("Power Drill");
        assertThat(requests.getFirst().getItems().getFirst().getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void getAllRequestsWhenUserNotFound() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L, pageRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");
    }

    @Test
    void getRequestByIdWhenRequestExists() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Need specific item");
        request.setRequester(anotherUser);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);

        Item item = new Item();
        item.setName("Requested Item");
        item.setDescription("Item for the request");
        item.setAvailable(true);
        item.setOwner(requester);
        item.setRequest(request);
        itemRepository.save(item);

        ItemRequestResponseDto result = itemRequestService.getRequestById(requester.getId(), request.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo("Need specific item");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getName()).isEqualTo("Requested Item");
    }

    @Test
    void getRequestByIdWhenRequestNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequestById(requester.getId(), 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Запрос с ID=999 не найден");
    }

    @Test
    void getRequestByIdWhenUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, 1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");
    }

    @Test
    void getRequestByIdWhenRequestHasNoItems() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Empty request");
        request.setRequester(anotherUser);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);

        ItemRequestResponseDto result = itemRequestService.getRequestById(requester.getId(), request.getId());

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getRequestByIdWhenRequestorViewsOwnRequest() {
        ItemRequest request = new ItemRequest();
        request.setDescription("My request");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);

        Item item = new Item();
        item.setName("Item for my request");
        item.setDescription("Someone responded to my request");
        item.setAvailable(true);
        item.setOwner(anotherUser);
        item.setRequest(request);
        itemRepository.save(item);

        ItemRequestResponseDto result = itemRequestService.getRequestById(requester.getId(), request.getId());

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getName()).isEqualTo("Item for my request");
    }

    @Test
    void getAllRequestsWithPagination() {
        for (int i = 1; i <= 15; i++) {
            ItemRequest request = new ItemRequest();
            request.setDescription("Request " + i);
            request.setRequester(anotherUser);
            request.setCreated(LocalDateTime.now().plusHours(i));
            itemRequestRepository.save(request);
        }

        PageRequest firstPage = PageRequest.of(0, 5);
        List<ItemRequestResponseDto> firstPageResults = itemRequestService.getAllRequests(requester.getId(), firstPage);

        assertThat(firstPageResults).hasSize(5);

        PageRequest secondPage = PageRequest.of(1, 5);
        List<ItemRequestResponseDto> secondPageResults = itemRequestService.getAllRequests(requester.getId(), secondPage);

        assertThat(secondPageResults).hasSize(5);
    }
}
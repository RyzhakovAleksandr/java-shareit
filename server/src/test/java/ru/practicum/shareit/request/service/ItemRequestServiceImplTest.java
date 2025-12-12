package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requester;
    private User anotherUser;
    private ItemRequestCreateDto createDto;
    private ItemRequest itemRequest;
    private Item item;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setId(1L);
        requester.setName("Requester");
        requester.setEmail("requester@example.com");

        anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@example.com");

        createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need a drill");

        itemRequest = new ItemRequest();
        itemRequest.setId(1L);
        itemRequest.setDescription("Need a drill");
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());

        item = new Item();
        item.setId(1L);
        item.setName("Power Drill");
        item.setDescription("Professional power drill");
        item.setAvailable(true);
        item.setOwner(anotherUser);
        item.setRequest(itemRequest);
    }

    @Test
    void createRequestWhenValidData() {
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestResponseDto result = itemRequestService.createRequest(requester.getId(), createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getItems()).isEmpty();

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequestWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.createRequest(999L, createDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");

        verify(userRepository).findById(eq(999L));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void createRequestWhenDescriptionIsBlank() {
        createDto.setDescription("   ");
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> itemRequestService.createRequest(requester.getId(), createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Описание запроса не может быть пустым");

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void createRequestWhenDescriptionIsNull() {
        createDto.setDescription(null);
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> itemRequestService.createRequest(requester.getId(), createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Описание запроса не может быть пустым");

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository, never()).save(any());
    }

    @Test
    void getUserRequestsWhenUserHasRequests() {
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(eq(requester.getId())))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findAllByRequestId(eq(itemRequest.getId()))).thenReturn(List.of(item));

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(requester.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getItems()).hasSize(1);
        assertThat(result.getFirst().getItems().getFirst().getName()).isEqualTo("Power Drill");

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedDesc(eq(requester.getId()));
        verify(itemRepository).findAllByRequestId(eq(itemRequest.getId()));
    }

    @Test
    void getUserRequestsWhenUserHasNoRequests() {
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(eq(requester.getId())))
                .thenReturn(Collections.emptyList());

        List<ItemRequestResponseDto> result = itemRequestService.getUserRequests(requester.getId());

        assertThat(result).isEmpty();

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).findAllByRequesterIdOrderByCreatedDesc(eq(requester.getId()));
        verify(itemRepository, never()).findAllByRequestId(any());
    }

    @Test
    void getUserRequestsWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getUserRequests(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");

        verify(userRepository).findById(eq(999L));
        verify(itemRequestRepository, never()).findAllByRequesterIdOrderByCreatedDesc(any());
    }

    @Test
    void getAllRequestsWhenOtherUsersHaveRequests() {
        ItemRequest otherRequest = new ItemRequest();
        otherRequest.setId(2L);
        otherRequest.setDescription("Other request");
        otherRequest.setRequester(anotherUser);
        otherRequest.setCreated(LocalDateTime.now());

        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdNot(eq(requester.getId()), any(Pageable.class)))
                .thenReturn(List.of(otherRequest));
        when(itemRepository.findAllByRequestId(eq(otherRequest.getId()))).thenReturn(Collections.emptyList());

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(requester.getId(), pageRequest);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(2L);
        assertThat(result.getFirst().getDescription()).isEqualTo("Other request");
        assertThat(result.getFirst().getItems()).isEmpty();

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).findAllByRequesterIdNot(eq(requester.getId()), eq(pageRequest));
        verify(itemRepository).findAllByRequestId(eq(otherRequest.getId()));
    }

    @Test
    void getAllRequestsWhenNoOtherRequests() {
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findAllByRequesterIdNot(eq(requester.getId()), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(requester.getId(), pageRequest);

        assertThat(result).isEmpty();

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).findAllByRequesterIdNot(eq(requester.getId()), eq(pageRequest));
        verify(itemRepository, never()).findAllByRequestId(any());
    }

    @Test
    void getAllRequestsWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        PageRequest pageRequest = PageRequest.of(0, 10);

        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L, pageRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");

        verify(userRepository).findById(eq(999L));
        verify(itemRequestRepository, never()).findAllByRequesterIdNot(any(), any());
    }

    @Test
    void getRequestByIdWhenRequestExists_() {
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(eq(itemRequest.getId()))).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(eq(itemRequest.getId()))).thenReturn(List.of(item));

        ItemRequestResponseDto result = itemRequestService.getRequestById(requester.getId(), itemRequest.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getName()).isEqualTo("Power Drill");

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).findById(eq(itemRequest.getId()));
        verify(itemRepository).findAllByRequestId(eq(itemRequest.getId()));
    }

    @Test
    void getRequestByIdWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, 1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID=999 не найден");

        verify(userRepository).findById(eq(999L));
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void getRequestByIdWhenRequestNotFound() {
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemRequestService.getRequestById(requester.getId(), 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Запрос с ID=999 не найден");

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).findById(eq(999L));
        verify(itemRepository, never()).findAllByRequestId(any());
    }

    @Test
    void getRequestByIdWhenRequestHasNoItems() {
        when(userRepository.findById(eq(requester.getId()))).thenReturn(Optional.of(requester));
        when(itemRequestRepository.findById(eq(itemRequest.getId()))).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequestId(eq(itemRequest.getId()))).thenReturn(Collections.emptyList());

        ItemRequestResponseDto result = itemRequestService.getRequestById(requester.getId(), itemRequest.getId());

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();

        verify(userRepository).findById(eq(requester.getId()));
        verify(itemRequestRepository).findById(eq(itemRequest.getId()));
        verify(itemRepository).findAllByRequestId(eq(itemRequest.getId()));
    }
}
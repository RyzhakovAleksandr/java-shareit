package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
    }

    @Test
    void createItemWhenValidData() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Item");

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItemWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(itemDto, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");

        verify(userRepository).findById(999L);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItemWhenNameIsBlank() {
        itemDto.setName("   ");

        assertThatThrownBy(() -> itemService.createItem(itemDto, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Название не может быть пустым");

        verify(userRepository, never()).findById(any());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItemWithRequestId() {

        ItemRequest request = new ItemRequest();
        request.setId(10L);

        itemDto.setRequestId(10L);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto, owner.getId());

        assertThat(result).isNotNull();
        verify(itemRequestRepository).findById(10L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItemWhenOwnerUpdates() {
        Item existingItem = new Item();
        existingItem.setId(1L);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner);

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("New Name");
        updatedItem.setDescription("New Description");
        updatedItem.setAvailable(false);
        updatedItem.setOwner(owner);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");
        updateDto.setDescription("New Description");
        updateDto.setAvailable(false);

        ItemDto result = itemService.updateItem(1L, updateDto, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getAvailable()).isFalse();

        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItemWhenNotOwnerUpdates() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Hacked Name");

        assertThatThrownBy(() -> itemService.updateItem(1L, updateDto, booker.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Только владелец может редактировать вещь");

        verify(itemRepository).findById(1L);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void getItemByIdWhenOwnerRequests() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Booking pastBooking = new Booking();
        pastBooking.setId(10L);
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);

        Booking futureBooking = new Booking();
        futureBooking.setId(11L);
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findPastBookingsForItem(1L)).thenReturn(List.of(pastBooking));
        when(bookingRepository.findFutureBookingsForItem(1L)).thenReturn(List.of(futureBooking));
        when(commentRepository.findCommentDtoByItemId(1L)).thenReturn(Collections.emptyList());

        ItemDtoWithBookings result = itemService.getItemById(1L, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();

        verify(itemRepository).findById(1L);
        verify(bookingRepository).findPastBookingsForItem(1L);
        verify(bookingRepository).findFutureBookingsForItem(1L);
        verify(commentRepository).findCommentDtoByItemId(1L);
    }

    @Test
    void getItemByIdWhenNotOwnerRequests() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findCommentDtoByItemId(1L)).thenReturn(Collections.emptyList());

        ItemDtoWithBookings result = itemService.getItemById(1L, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();

        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).findPastBookingsForItem(any());
        verify(bookingRepository, never()).findFutureBookingsForItem(any());
        verify(commentRepository).findCommentDtoByItemId(1L);
    }

    @Test
    void getItemsByOwnerWhenItemsExist() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdWithRequest(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository.findPastBookingsByItemIds(anyList())).thenReturn(Collections.emptyList());
        when(bookingRepository.findFutureBookingsByItemIds(anyList())).thenReturn(Collections.emptyList());
        when(commentRepository.findByItemIdsWithAuthor(anyList())).thenReturn(Collections.emptyList());

        List<ItemDtoWithBookings> result = itemService.getItemsByOwner(owner.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);

        verify(userRepository).findById(owner.getId());
        verify(itemRepository).findByOwnerIdWithRequest(owner.getId());
    }

    @Test
    void searchItemsWhenTextMatches() {
        when(itemRepository.searchWithRequest("drill")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Test Item");

        verify(itemRepository).searchWithRequest("drill");
    }

    @Test
    void searchItemsWhenTextIsBlank() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertThat(result).isEmpty();
        verify(itemRepository, never()).searchWithRequest(any());
    }

    @Test
    void addCommentWhenValidComment() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Great item!");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(1L, booker.getId()))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.addComment(1L, commentCreateDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Great item!");

        verify(itemRepository).findById(1L);
        verify(userRepository).findById(booker.getId());
        verify(bookingRepository).existsByItemIdAndBookerIdAndEndBefore(1L, booker.getId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addCommentWhenUserNeverBooked() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Trying to comment");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(1L, booker.getId()))
                .thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(1L, commentCreateDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь не брал вещь в аренду или аренда еще не завершена");

        verify(commentRepository, never()).save(any());
    }
}
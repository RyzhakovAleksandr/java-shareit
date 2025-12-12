package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentDto;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User booker;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
    }

    @Test
    void createItemWhenValidData() {
        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        assertThat(createdItem).isNotNull();
        assertThat(createdItem.getId()).isNotNull();
        assertThat(createdItem.getName()).isEqualTo("Test Item");
        assertThat(createdItem.getDescription()).isEqualTo("Test Description");
        assertThat(createdItem.getAvailable()).isTrue();

        Item savedItem = itemRepository.findById(createdItem.getId()).orElseThrow();
        assertThat(savedItem.getName()).isEqualTo("Test Item");
        assertThat(savedItem.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void createItemWhenUserNotFound() {
        assertThatThrownBy(() -> itemService.createItem(itemDto, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");
    }

    @Test
    void createItemWhenNameIsBlank() {
        itemDto.setName("   ");
        assertThatThrownBy(() -> itemService.createItem(itemDto, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Название не может быть пустым");
    }

    @Test
    void createItemWithRequestId() {
        ItemRequest request = new ItemRequest();
        request.setDescription("Need an item");
        request.setRequester(booker);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);

        itemDto.setRequestId(request.getId());

        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        assertThat(createdItem).isNotNull();
        assertThat(createdItem.getRequestId()).isEqualTo(request.getId());

        Item savedItem = itemRepository.findById(createdItem.getId()).orElseThrow();
        assertThat(savedItem.getRequest().getId()).isEqualTo(request.getId());
    }

    @Test
    void updateItemWhenOwnerUpdates() {
        Item item = new Item();
        item.setName("Old Name");
        item.setDescription("Old Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");
        updateDto.setDescription("New Description");
        updateDto.setAvailable(false);

        ItemDto updatedItem = itemService.updateItem(item.getId(), updateDto, owner.getId());

        assertThat(updatedItem).isNotNull();
        assertThat(updatedItem.getName()).isEqualTo("New Name");
        assertThat(updatedItem.getDescription()).isEqualTo("New Description");
        assertThat(updatedItem.getAvailable()).isFalse();

        Item dbItem = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(dbItem.getName()).isEqualTo("New Name");
        assertThat(dbItem.isAvailable()).isFalse();
    }

    @Test
    void updateItemWhenNotOwnerUpdates() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Hacked Name");

        Item finalItem = item;
        assertThatThrownBy(() -> itemService.updateItem(finalItem.getId(), updateDto, booker.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Только владелец может редактировать вещь");
    }

    @Test
    void getItemByIdWhenOwnerRequests() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking();
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("Great item!");

        Booking completedBooking = new Booking();
        completedBooking.setStart(LocalDateTime.now().minusDays(10));
        completedBooking.setEnd(LocalDateTime.now().minusDays(5));
        completedBooking.setItem(item);
        completedBooking.setBooker(booker);
        completedBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(completedBooking);

        ItemDtoWithBookings result = itemService.getItemById(item.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getLastBooking().getBookerId()).isEqualTo(booker.getId());
        assertThat(result.getNextBooking().getBookerId()).isEqualTo(booker.getId());
    }

    @Test
    void getItemByIdWhenNotOwnerRequests() {

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        ItemDtoWithBookings result = itemService.getItemById(item.getId(), booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemsByOwnerWhenOwnerHasItems() {
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Desc 1");
        item1.setAvailable(true);
        item1.setOwner(owner);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Desc 2");
        item2.setAvailable(false);
        item2.setOwner(owner);
        itemRepository.save(item2);

        List<ItemDtoWithBookings> result = itemService.getItemsByOwner(owner.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemDtoWithBookings::getName)
                .containsExactlyInAnyOrder("Item 1", "Item 2");
    }

    @Test
    void searchItemsWhenTextMatches() {
        Item item = new Item();
        item.setName("Drill Machine");
        item.setDescription("Powerful electric drill");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        Item unavailableItem = new Item();
        unavailableItem.setName("Old Drill");
        unavailableItem.setDescription("Broken drill");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        List<ItemDto> result = itemService.searchItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Drill Machine");
    }

    @Test
    void searchItemsWhenTextIsBlank() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteItem_whenOwnerDeletes_thenItemDeleted() {
        Item item = new Item();
        item.setName("Item to delete");
        item.setDescription("Will be deleted");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        itemService.deleteItem(item.getId(), owner.getId());

        assertThat(itemRepository.existsById(item.getId())).isFalse();
    }

    @Test
    void deleteItemWhenNotOwnerDeletes() {
        Item item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        Item finalItem = item;
        assertThatThrownBy(() -> itemService.deleteItem(finalItem.getId(), booker.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Только владелец может удалить вещь");
    }

    @Test
    void addCommentWhenBookerAddsComment() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(10));
        booking.setEnd(LocalDateTime.now().minusDays(5));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("Great item, thanks!");

        CommentDto result = itemService.addComment(item.getId(), commentDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Great item, thanks!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");

        List<CommentDto> comments = commentRepository.findCommentDtoByItemId(item.getId());
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getText()).isEqualTo("Great item, thanks!");
    }

    @Test
    void addCommentWhenUserNeverBooked() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("Trying to comment without booking");

        Item finalItem = item;
        assertThatThrownBy(() -> itemService.addComment(finalItem.getId(), commentDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Пользователь не брал вещь в аренду или аренда еще не завершена");
    }
}
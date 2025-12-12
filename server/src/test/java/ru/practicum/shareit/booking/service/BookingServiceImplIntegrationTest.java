package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item availableItem;
    private Item unavailableItem;
    private BookingRequestDto bookingRequestDto;

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

        availableItem = new Item();
        availableItem.setName("Available Item");
        availableItem.setDescription("Available for booking");
        availableItem.setAvailable(true);
        availableItem.setOwner(owner);
        availableItem = itemRepository.save(availableItem);

        unavailableItem = new Item();
        unavailableItem.setName("Unavailable Item");
        unavailableItem.setDescription("Not available for booking");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        unavailableItem = itemRepository.save(unavailableItem);

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(availableItem.getId());
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    void createBookingWhenValidData() {
        BookingDto createdBooking = bookingService.createBooking(bookingRequestDto, booker.getId());

        assertThat(createdBooking).isNotNull();
        assertThat(createdBooking.getId()).isNotNull();
        assertThat(createdBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(createdBooking.getItem().getId()).isEqualTo(availableItem.getId());
        assertThat(createdBooking.getBooker().getId()).isEqualTo(booker.getId());

        Booking savedBooking = bookingRepository.findById(createdBooking.getId()).orElseThrow();
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(savedBooking.getItem().getId()).isEqualTo(availableItem.getId());
    }

    @Test
    void createBookingWhenUserNotFound() {
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");
    }

    @Test
    void createBookingWhenItemNotFound() {
        bookingRequestDto.setItemId(999L);

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Вещь с ID 999 не найдена");
    }

    @Test
    void createBookingWhenItemNotAvailable() {
        bookingRequestDto.setItemId(unavailableItem.getId());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Вещь недоступна для бронирования");
    }

    @Test
    void createBookingWhenOwnerBooksOwnItem() {
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нельзя бронировать свою же вещь");
    }

    @Test
    void createBookingWhenStartAfterEnd() {
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(3));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректные даты бронирования");
    }

    @Test
    void createBookingWhenStartEqualsEnd() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        bookingRequestDto.setStart(sameTime);
        bookingRequestDto.setEnd(sameTime);

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректные даты бронирования");
    }

    @Test
    void createBookingWhenStartInPast() {
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нельзя бронировать в прошлом");
    }

    @Test
    void approveBookingWhenOwnerApproves() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);

        BookingDto approvedBooking = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        assertThat(approvedBooking).isNotNull();
        assertThat(approvedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);

        Booking dbBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertThat(dbBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBookingWhenOwnerRejects() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);

        BookingDto rejectedBooking = bookingService.approveBooking(booking.getId(), owner.getId(), false);

        assertThat(rejectedBooking).isNotNull();
        assertThat(rejectedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveBookingWhenNotOwnerApproves() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
        booking = bookingRepository.save(booking);

        User anotherUser = new User();
        anotherUser.setName("Another");
        anotherUser.setEmail("another@example.com");
        anotherUser = userRepository.save(anotherUser);

        Booking finalBooking = booking;
        User finalAnotherUser = anotherUser;
        assertThatThrownBy(() -> bookingService.approveBooking(finalBooking.getId(), finalAnotherUser.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Только владелец вещи может подтверждать бронирование");
    }

    @Test
    void approveBookingWhenAlreadyApproved() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        Booking finalBooking = booking;
        assertThatThrownBy(() -> bookingService.approveBooking(finalBooking.getId(), owner.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Бронирование уже обработано");
    }

    @Test
    void getBookingByIdWhenBookerRequests() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        BookingDto result = bookingService.getBookingById(booking.getId(), booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingByIdWhenOwnerRequests() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        BookingDto result = bookingService.getBookingById(booking.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingByIdWhenUnauthorizedUserRequests() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(availableItem);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        User stranger = new User();
        stranger.setName("Stranger");
        stranger.setEmail("stranger@example.com");
        stranger = userRepository.save(stranger);

        Booking finalBooking = booking;
        User finalStranger = stranger;
        assertThatThrownBy(() -> bookingService.getBookingById(finalBooking.getId(), finalStranger.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нет доступа к данному бронированию");
    }

    @Test
    void getBookingsByBookerWhenAllState() {
        Booking booking1 = new Booking();
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));
        booking1.setItem(availableItem);
        booking1.setBooker(booker);
        booking1.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking1);

        Booking booking2 = new Booking();
        booking2.setStart(LocalDateTime.now().plusDays(3));
        booking2.setEnd(LocalDateTime.now().plusDays(4));
        booking2.setItem(availableItem);
        booking2.setBooker(booker);
        booking2.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking2);

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.ALL);

        assertThat(result).hasSize(2);
    }

    @Test
    void getBookingsByBookerWhenCurrentState() {
        Booking currentBooking = new Booking();
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        currentBooking.setItem(availableItem);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(currentBooking);

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.CURRENT);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBookerWhenPastState_thenReturnPastBookings() {
        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(3));
        pastBooking.setEnd(LocalDateTime.now().minusDays(2));
        pastBooking.setItem(availableItem);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.PAST);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBookerWhenFutureState_thenReturnFutureBookings() {
        Booking futureBooking = new Booking();
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setItem(availableItem);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.FUTURE);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBookerWhenWaitingState() {
        Booking waitingBooking = new Booking();
        waitingBooking.setStart(LocalDateTime.now().plusDays(1));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(2));
        waitingBooking.setItem(availableItem);
        waitingBooking.setBooker(booker);
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(waitingBooking);

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getBookingsByBookerWhenRejectedState() {

        Booking rejectedBooking = new Booking();
        rejectedBooking.setStart(LocalDateTime.now().plusDays(1));
        rejectedBooking.setEnd(LocalDateTime.now().plusDays(2));
        rejectedBooking.setItem(availableItem);
        rejectedBooking.setBooker(booker);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getBookingsByOwnerWhenAllState() {
        Booking booking1 = new Booking();
        booking1.setStart(LocalDateTime.now().plusDays(1));
        booking1.setEnd(LocalDateTime.now().plusDays(2));
        booking1.setItem(availableItem);
        booking1.setBooker(booker);
        booking1.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking1);

        List<BookingDto> result = bookingService.getBookingsByOwner(owner.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getItem().getId()).isEqualTo(availableItem.getId());
    }

    @Test
    void getBookingsByOwnerWhenUserNotFound() {
        assertThatThrownBy(() -> bookingService.getBookingsByOwner(999L, BookingState.ALL))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");
    }
}
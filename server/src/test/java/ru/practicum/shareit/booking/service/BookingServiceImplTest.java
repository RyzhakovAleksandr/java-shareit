package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private BookingRequestDto bookingRequestDto;
    private Booking booking;

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

        bookingRequestDto = new BookingRequestDto();
        bookingRequestDto.setItemId(1L);
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void createBookingWhenValidData() {
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.createBooking(bookingRequestDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);

        verify(userRepository).findById(eq(booker.getId()));
        verify(itemRepository).findById(eq(item.getId()));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBookingWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");

        verify(userRepository).findById(eq(999L));
        verify(itemRepository, never()).findById(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenItemNotFound() {
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Вещь с ID 1 не найдена");

        verify(userRepository).findById(eq(booker.getId()));
        verify(itemRepository).findById(eq(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Вещь недоступна для бронирования");

        verify(userRepository).findById(eq(booker.getId()));
        verify(itemRepository).findById(eq(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenOwnerBooksOwnItem() {
        when(userRepository.findById(eq(owner.getId()))).thenReturn(Optional.of(owner));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, owner.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нельзя бронировать свою же вещь");

        verify(userRepository).findById(eq(owner.getId()));
        verify(itemRepository).findById(eq(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenStartAfterEnd() {
        bookingRequestDto.setStart(LocalDateTime.now().plusDays(3));
        bookingRequestDto.setEnd(LocalDateTime.now().plusDays(2));

        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректные даты бронирования");

        verify(userRepository).findById(eq(booker.getId()));
        verify(itemRepository).findById(eq(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenStartInPast() {
        bookingRequestDto.setStart(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(item.getId()))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingRequestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нельзя бронировать в прошлом");

        verify(userRepository).findById(eq(booker.getId()));
        verify(itemRepository).findById(eq(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBookingWhenOwnerApproves() {
        Booking approvedBooking = new Booking();
        approvedBooking.setId(1L);
        approvedBooking.setItem(item);
        approvedBooking.setBooker(booker);
        approvedBooking.setStart(booking.getStart());
        approvedBooking.setEnd(booking.getEnd());
        approvedBooking.setStatus(BookingStatus.APPROVED);

        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(approvedBooking);

        BookingDto result = bookingService.approveBooking(1L, owner.getId(), true);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);

        verify(bookingRepository).findById(eq(1L));
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approveBookingWhenNotOwnerApproves() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 999L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Только владелец вещи может подтверждать бронирование");

        verify(bookingRepository).findById(eq(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBookingWhenAlreadyApproved() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, owner.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Бронирование уже обработано");

        verify(bookingRepository).findById(eq(1L));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void getBookingByIdWhenBookerRequests() {
        when(bookingRepository.findByIdWithRelations(eq(1L))).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(1L, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(bookingRepository).findByIdWithRelations(eq(1L));
    }

    @Test
    void getBookingByIdWhenOwnerRequests() {
        when(bookingRepository.findByIdWithRelations(eq(1L))).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBookingById(1L, owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(bookingRepository).findByIdWithRelations(eq(1L));
    }

    @Test
    void getBookingByIdWhenUnauthorizedUserRequests() {
        when(bookingRepository.findByIdWithRelations(eq(1L))).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 999L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нет доступа к данному бронированию");

        verify(bookingRepository).findByIdWithRelations(eq(1L));
    }

    @Test
    void getBookingsByBookerWhenAllState() {
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdWithRelations(eq(booker.getId()), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);

        verify(userRepository).findById(eq(booker.getId()));
        verify(bookingRepository).findByBookerIdWithRelations(eq(booker.getId()), any(Sort.class));
    }

    @Test
    void getBookingsByBookerWhenCurrentState() {

        LocalDateTime now = LocalDateTime.now();
        booking.setStart(now.minusDays(1));
        booking.setEnd(now.plusDays(1));

        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterWithRelations(
                eq(booker.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.CURRENT);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void getBookingsByBookerWhenPastState() {
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndEndBeforeWithRelations(
                eq(booker.getId()), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.PAST);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBookerWhenFutureState() {
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartAfterWithRelations(
                eq(booker.getId()), any(LocalDateTime.class), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.FUTURE);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBookerWhenWaitingState() {
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatusWithRelations(
                eq(booker.getId()), eq(BookingStatus.WAITING), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getBookingsByBookerWhenRejectedState() {
        booking.setStatus(BookingStatus.REJECTED);

        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatusWithRelations(
                eq(booker.getId()), eq(BookingStatus.REJECTED), any(Sort.class)))
                .thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getBookingsByBooker(booker.getId(), BookingState.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getBookingsByBookerWhenInvalidState() {
        when(userRepository.findById(eq(booker.getId()))).thenReturn(Optional.of(booker));

        assertThatThrownBy(() -> bookingService.getBookingsByBooker(booker.getId(), null))
                .isInstanceOf(NullPointerException.class);

        verify(userRepository).findById(eq(booker.getId()));
    }
}
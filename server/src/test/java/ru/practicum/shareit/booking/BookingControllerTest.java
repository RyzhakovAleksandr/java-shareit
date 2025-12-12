package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void createBooking() {
        BookingRequestDto requestDto = new BookingRequestDto();
        BookingDto expected = new BookingDto();
        when(bookingService.createBooking(requestDto, 1L)).thenReturn(expected);

        BookingDto result = bookingController.createBooking(requestDto, 1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getBookingsByBooker() {
        List<BookingDto> expected = Collections.singletonList(new BookingDto());
        when(bookingService.getBookingsByBooker(1L, BookingState.ALL)).thenReturn(expected);

        List<BookingDto> result = bookingController.getBookingsByBooker(1L, BookingState.ALL, 0, 10);

        assertThat(result).isEqualTo(expected);
    }
}
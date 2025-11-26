package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingDto createBooking(@RequestBody BookingDto bookingDto,
                                    @RequestHeader(USER_ID_HEADER) Long bookerId) {
        return bookingService.createBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable Long bookingId,
                                     @RequestParam boolean approved,
                                     @RequestHeader(USER_ID_HEADER) Long ownerId) {
        return bookingService.approveBooking(bookingId, ownerId, approved);
    }

    @GetMapping
    public List<BookingDto> getBookingsByBooker(@RequestHeader(USER_ID_HEADER) Long bookerId) {
        return bookingService.getBookingsByBooker(bookerId);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        return bookingService.getBookingsByOwner(ownerId);
    }
}

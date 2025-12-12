package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoWithBookingsTest {

    @Test
    void shouldCreateEmptyItemDtoWithBookings() {
        ItemDtoWithBookings dto = new ItemDtoWithBookings();

        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getAvailable());
        assertNull(dto.getRequestId());
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        assertNull(dto.getComments());
    }

    @Test
    void shouldCreateItemDtoWithBookingsWithAllArgsConstructor() {
        Long id = 1L;
        String name = "Test Item";
        String description = "Test Description";
        Boolean available = true;
        Long requestId = 10L;

        LocalDateTime now = LocalDateTime.now();
        ItemDtoWithBookings.BookingInfo lastBooking = new ItemDtoWithBookings.BookingInfo(
                100L, 200L, now.minusDays(2), now.minusDays(1)
        );
        ItemDtoWithBookings.BookingInfo nextBooking = new ItemDtoWithBookings.BookingInfo(
                101L, 201L, now.plusDays(1), now.plusDays(2)
        );

        List<CommentDto> comments = List.of(
                new CommentDto(1L, "Great item!", "User1", now.minusHours(5)),
                new CommentDto(2L, "Works perfectly", "User2", now.minusHours(3))
        );

        ItemDtoWithBookings dto = new ItemDtoWithBookings(
                id, name, description, available, requestId,
                lastBooking, nextBooking, comments
        );

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(available, dto.getAvailable());
        assertEquals(requestId, dto.getRequestId());
        assertEquals(lastBooking, dto.getLastBooking());
        assertEquals(nextBooking, dto.getNextBooking());
        assertEquals(comments, dto.getComments());
    }

    @Test
    void shouldSetAndGetAllFields() {
        ItemDtoWithBookings dto = new ItemDtoWithBookings();
        Long id = 2L;
        String name = "Updated Item";
        String description = "Updated Description";
        Boolean available = false;
        Long requestId = 20L;

        LocalDateTime now = LocalDateTime.now();
        ItemDtoWithBookings.BookingInfo lastBooking = new ItemDtoWithBookings.BookingInfo(
                300L, 400L, now.minusDays(5), now.minusDays(4)
        );
        ItemDtoWithBookings.BookingInfo nextBooking = new ItemDtoWithBookings.BookingInfo(
                301L, 401L, now.plusDays(3), now.plusDays(4)
        );

        List<CommentDto> comments = List.of(
                new CommentDto(3L, "Not bad", "User3", now.minusHours(2))
        );

        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        dto.setRequestId(requestId);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(description, dto.getDescription());
        assertEquals(available, dto.getAvailable());
        assertEquals(requestId, dto.getRequestId());
        assertEquals(lastBooking, dto.getLastBooking());
        assertEquals(nextBooking, dto.getNextBooking());
        assertEquals(comments, dto.getComments());
    }

    @Test
    void shouldHandleNullBookingsAndComments() {
        ItemDtoWithBookings dto = new ItemDtoWithBookings(
                1L, "Item", "Desc", true, 5L,
                null, null, null
        );

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getLastBooking());
        assertNull(dto.getNextBooking());
        assertNull(dto.getComments());
    }

    @Test
    void shouldReturnCorrectToString() {
        ItemDtoWithBookings dto = new ItemDtoWithBookings(
                1L, "Drill", "Powerful drill", true, 10L,
                null, null, null
        );

        String stringRepresentation = dto.toString();

        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("Drill"));
        assertTrue(stringRepresentation.contains("Powerful drill"));
        assertTrue(stringRepresentation.contains("id=1"));
    }

    @Test
    void testEqualsAndHashCode() {

        LocalDateTime now = LocalDateTime.now();
        ItemDtoWithBookings.BookingInfo booking1 = new ItemDtoWithBookings.BookingInfo(1L, 10L, now, now.plusDays(1));
        ItemDtoWithBookings.BookingInfo booking2 = new ItemDtoWithBookings.BookingInfo(1L, 10L, now, now.plusDays(1));

        List<CommentDto> comments1 = List.of(new CommentDto(1L, "Good", "User", now));
        List<CommentDto> comments2 = List.of(new CommentDto(1L, "Good", "User", now));

        ItemDtoWithBookings dto1 = new ItemDtoWithBookings(
                1L, "Item", "Desc", true, 5L, booking1, null, comments1
        );
        ItemDtoWithBookings dto2 = new ItemDtoWithBookings(
                1L, "Item", "Desc", true, 5L, booking2, null, comments2
        );
        ItemDtoWithBookings dto3 = new ItemDtoWithBookings(
                2L, "Another", "Diff", false, 6L, null, null, null
        );

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void shouldCreateBookingInfo() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 20, 10, 0);

        ItemDtoWithBookings.BookingInfo bookingInfo = new ItemDtoWithBookings.BookingInfo(
                1L, 100L, start, end
        );

        assertNotNull(bookingInfo);
        assertEquals(1L, bookingInfo.getId());
        assertEquals(100L, bookingInfo.getBookerId());
        assertEquals(start, bookingInfo.getStart());
        assertEquals(end, bookingInfo.getEnd());
    }

    @Test
    void shouldSetAndGetBookingInfoFields() {
        ItemDtoWithBookings.BookingInfo bookingInfo = new ItemDtoWithBookings.BookingInfo();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(3);

        bookingInfo.setId(5L);
        bookingInfo.setBookerId(50L);
        bookingInfo.setStart(start);
        bookingInfo.setEnd(end);

        assertEquals(5L, bookingInfo.getId());
        assertEquals(50L, bookingInfo.getBookerId());
        assertEquals(start, bookingInfo.getStart());
        assertEquals(end, bookingInfo.getEnd());
    }

    @Test
    void testBookingInfoEqualsAndHashCode() {

        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 20, 10, 0);

        ItemDtoWithBookings.BookingInfo booking1 = new ItemDtoWithBookings.BookingInfo(1L, 10L, start, end);
        ItemDtoWithBookings.BookingInfo booking2 = new ItemDtoWithBookings.BookingInfo(1L, 10L, start, end);
        ItemDtoWithBookings.BookingInfo booking3 = new ItemDtoWithBookings.BookingInfo(2L, 20L, start.plusDays(1), end.plusDays(1));

        assertEquals(booking1, booking2);
        assertNotEquals(booking1, booking3);
        assertEquals(booking1.hashCode(), booking2.hashCode());
        assertNotEquals(booking1.hashCode(), booking3.hashCode());
    }

    @Test
    void shouldReturnBookingInfoToString() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 20, 10, 0);
        ItemDtoWithBookings.BookingInfo bookingInfo = new ItemDtoWithBookings.BookingInfo(1L, 10L, start, end);

        String stringRep = bookingInfo.toString();

        assertNotNull(stringRep);
        assertTrue(stringRep.contains("id=1"));
        assertTrue(stringRep.contains("bookerId=10"));
    }

    @Test
    void shouldHandleEmptyCommentsList() {
        ItemDtoWithBookings dto = new ItemDtoWithBookings(
                1L, "Item", "Desc", true, null,
                null, null, List.of()
        );

        assertNotNull(dto.getComments());
        assertTrue(dto.getComments().isEmpty());
    }

    @Test
    void shouldCreateWithRequestIdNull() {
        ItemDtoWithBookings dto = new ItemDtoWithBookings(
                1L, "Item", "Description", true, null,
                null, null, null
        );

        assertNull(dto.getRequestId());
        assertTrue(dto.getAvailable());
    }

    @Test
    void shouldHandleEdgeCasesForBookingDates() {
        LocalDateTime minDateTime = LocalDateTime.MIN;
        LocalDateTime maxDateTime = LocalDateTime.MAX;

        ItemDtoWithBookings.BookingInfo booking = new ItemDtoWithBookings.BookingInfo(
                1L, 1L, minDateTime, maxDateTime
        );

        assertEquals(minDateTime, booking.getStart());
        assertEquals(maxDateTime, booking.getEnd());
    }
}
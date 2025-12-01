package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoWithBookings {
    Long id;
    String name;
    String description;
    Boolean available;
    Long requestId;
    BookingInfo lastBooking;
    BookingInfo nextBooking;
    List<CommentDto> comments;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingInfo {
        Long id;
        Long bookerId;
        LocalDateTime start;
        LocalDateTime end;
    }
}

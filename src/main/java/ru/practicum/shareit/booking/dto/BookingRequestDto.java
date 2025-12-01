package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull(message = "ID вещи не может быть пустым значем")
    Long itemId;

    @NotNull(message = "Дата начала не может быть пустым значением")
    @FutureOrPresent(message = "Дата начала должна быть в настоящем или будущем")
    LocalDateTime start;

    @NotNull(message = "Дата завершения не может быть пустым значением")
    @Future(message = "Дата завершения должна быть в будущем")
    LocalDateTime end;
}
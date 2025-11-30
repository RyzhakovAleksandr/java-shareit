package ru.practicum.shareit.booking.repository;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    List<Booking> findByBookerId(Long bookerId);

    List<Booking> findByItemOwnerId(Long ownerId);

    List<Booking> findByItemId(Long itemId);

    void deleteById(Long id);
}

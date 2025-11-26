package ru.practicum.shareit.booking.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(idCounter++);
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }

    @Override
    public List<Booking> findByBookerId(Long bookerId) {
        return bookings.values().stream()
                .filter(booking -> booking.getBooker().getId().equals(bookerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByItemOwnerId(Long ownerId) {
        return bookings.values().stream()
                .filter(booking -> booking.getItem().getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByItemId(Long itemId) {
        return bookings.values().stream()
                .filter(booking -> booking.getItem().getId().equals(itemId))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        bookings.remove(id);
    }
}

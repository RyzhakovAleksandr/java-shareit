package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto createBooking(BookingDto bookingDto, Long bookerId) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", bookerId)));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Вещь с ID %d не найдена", bookingDto.getItemId())));

        if (!item.isAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(bookerId)) {
            throw new IllegalArgumentException("Нельзя бронировать свою же вещь");
        }

        if (bookingDto.getStart().isAfter(bookingDto.getEnd())
                || bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new IllegalArgumentException("Некорректные даты бронирования");
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя бронировать в прошлом");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, userRepository.findById(bookerId).get());
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Бронирование с ID %d не найдено", bookingId)));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Бронирование с ID %d не найдено", bookingId)));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Нет доступа к данному бронированию");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long bookerId) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Пользователь с ID %d не найден", bookerId)));

        return bookingRepository.findByBookerId(bookerId).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto cancelBooking(Long bookingId, Long bookerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Бронирование с ID %d не найдено", bookingId)));

        if (!booking.getBooker().getId().equals(bookerId)) {
            throw new IllegalArgumentException("Только создатель бронирования может его отменить");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Нельзя отменить уже обработанное бронирование");
        }

        booking.setStatus(BookingStatus.CANCELED);
        Booking canceledBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(canceledBooking);
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long ownerId) {
        return getBookingsByBooker(ownerId);
    }
}

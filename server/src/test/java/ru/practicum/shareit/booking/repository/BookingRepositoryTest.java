package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker1;
    private User booker2;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        owner = createUser("Owner", "owner@example.com");
        booker1 = createUser("Booker1", "booker1@example.com");
        booker2 = createUser("Booker2", "booker2@example.com");

        entityManager.persist(owner);
        entityManager.persist(booker1);
        entityManager.persist(booker2);
        entityManager.flush();

        item1 = createItem("Item 1", "Description 1", owner, true);
        item2 = createItem("Item 2", "Description 2", owner, true);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();
    }

    @Test
    void shouldSaveBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Booking booking = createBooking(start, end, item1, booker1, BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStart()).isEqualTo(start);
        assertThat(saved.getEnd()).isEqualTo(end);
        assertThat(saved.getItem()).isEqualTo(item1);
        assertThat(saved.getBooker()).isEqualTo(booker1);
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void shouldFindByBookerIdWithRelations() {
        LocalDateTime now = LocalDateTime.now();

        Booking booking1 = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking booking2 = createBooking(now.plusDays(3), now.plusDays(4), item2, booker1, BookingStatus.WAITING);
        Booking booking3 = createBooking(now.plusDays(5), now.plusDays(6), item1, booker2, BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);
        entityManager.flush();

        entityManager.clear();
        List<Booking> bookings = bookingRepository.findByBookerIdWithRelations(
                booker1.getId(),
                Sort.by(Sort.Direction.DESC, "start")
        );

        assertThat(bookings).hasSize(2);

        bookings.forEach(booking -> {
            assertThat(booking.getItem()).isNotNull();
            assertThat(booking.getBooker()).isNotNull();
            assertThat(booking.getBooker().getName()).isEqualTo("Booker1");
        });

        assertThat(bookings.get(0).getStart()).isAfter(bookings.get(1).getStart());
    }

    @Test
    void shouldFindByItemOwnerIdWithRelations() {
        LocalDateTime now = LocalDateTime.now();

        Booking booking1 = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking booking2 = createBooking(now.plusDays(3), now.plusDays(4), item2, booker2, BookingStatus.WAITING);
        Booking booking3 = createBooking(now.plusDays(5), now.plusDays(6), item1, booker2, BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);
        entityManager.flush();
        entityManager.clear();

        List<Booking> bookings = bookingRepository.findByItemOwnerIdWithRelations(
                owner.getId(),
                Sort.by(Sort.Direction.ASC, "start")
        );

        assertThat(bookings).hasSize(3);

        bookings.forEach(booking ->
                assertThat(booking.getItem().getOwner().getId()).isEqualTo(owner.getId())
        );

        assertThat(bookings.get(0).getStart()).isBefore(bookings.get(1).getStart());
    }

    @Test
    void shouldFindByBookerIdAndStatusWithRelations() {
        LocalDateTime now = LocalDateTime.now();

        Booking booking1 = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking booking2 = createBooking(now.plusDays(3), now.plusDays(4), item2, booker1, BookingStatus.WAITING);
        Booking booking3 = createBooking(now.plusDays(5), now.plusDays(6), item1, booker1, BookingStatus.REJECTED);
        Booking booking4 = createBooking(now.plusDays(7), now.plusDays(8), item2, booker2, BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);
        entityManager.persist(booking4);
        entityManager.flush();

        entityManager.clear();

        List<Booking> approvedBookings = bookingRepository.findByBookerIdAndStatusWithRelations(
                booker1.getId(),
                BookingStatus.APPROVED,
                Sort.by(Sort.Direction.DESC, "start")
        );

        assertThat(approvedBookings).hasSize(1);
        assertThat(approvedBookings.getFirst().getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(approvedBookings.getFirst().getBooker().getId()).isEqualTo(booker1.getId());
    }

    @Test
    void shouldFindByBookerIdAndEndBeforeWithRelations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusDays(1);

        Booking pastBooking = createBooking(now.minusDays(3), now.minusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking currentBooking = createBooking(now.minusDays(1), now.plusDays(1), item2, booker1, BookingStatus.APPROVED);
        Booking futureBooking = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.APPROVED);

        entityManager.persist(pastBooking);
        entityManager.persist(currentBooking);
        entityManager.persist(futureBooking);
        entityManager.flush();
        entityManager.clear();

        List<Booking> pastBookings = bookingRepository.findByBookerIdAndEndBeforeWithRelations(
                booker1.getId(),
                pastTime,
                Sort.by(Sort.Direction.DESC, "end")
        );

        assertThat(pastBookings).hasSize(1);
        assertThat(pastBookings.getFirst().getEnd()).isBefore(pastTime);
    }

    @Test
    void shouldFindByBookerIdAndStartAfterWithRelations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusDays(1);

        Booking pastBooking = createBooking(now.minusDays(2), now.minusDays(1), item1, booker1, BookingStatus.APPROVED);
        Booking currentBooking = createBooking(now.minusDays(1), now.plusDays(1), item2, booker1, BookingStatus.APPROVED);
        Booking futureBooking = createBooking(now.plusDays(2), now.plusDays(3), item1, booker1, BookingStatus.APPROVED);

        entityManager.persist(pastBooking);
        entityManager.persist(currentBooking);
        entityManager.persist(futureBooking);
        entityManager.flush();

        entityManager.clear();

        List<Booking> futureBookings = bookingRepository.findByBookerIdAndStartAfterWithRelations(
                booker1.getId(),
                futureTime,
                Sort.by(Sort.Direction.ASC, "start")
        );

        assertThat(futureBookings).hasSize(1);
        assertThat(futureBookings.getFirst().getStart()).isAfter(futureTime);
    }

    @Test
    void shouldFindByBookerIdAndStartBeforeAndEndAfterWithRelations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(2);
        LocalDateTime end = now.plusHours(2);

        Booking pastBooking = createBooking(now.minusDays(2), now.minusDays(1), item1, booker1, BookingStatus.APPROVED);
        Booking currentBooking = createBooking(now.minusHours(3), now.plusHours(3), item2, booker1, BookingStatus.APPROVED);
        Booking futureBooking = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.APPROVED);

        entityManager.persist(pastBooking);
        entityManager.persist(currentBooking);
        entityManager.persist(futureBooking);
        entityManager.flush();
        entityManager.clear();

        List<Booking> currentBookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterWithRelations(
                booker1.getId(),
                start,
                end,
                Sort.by(Sort.Direction.ASC, "start")
        );

        assertThat(currentBookings).hasSize(1);
        assertThat(currentBookings.getFirst().getStart()).isBefore(start);
        assertThat(currentBookings.getFirst().getEnd()).isAfter(end);
    }

    @Test
    void shouldFindByIdWithRelations() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.WAITING);

        entityManager.persist(booking);
        entityManager.flush();

        Long bookingId = booking.getId();
        entityManager.clear();

        Optional<Booking> found = bookingRepository.findByIdWithRelations(bookingId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(bookingId);
        assertThat(found.get().getItem()).isNotNull();
        assertThat(found.get().getBooker()).isNotNull();
    }

    @Test
    void shouldFindPastBookingsForItem() {
        LocalDateTime now = LocalDateTime.now();

        Booking pastBooking1 = createBooking(now.minusDays(3), now.minusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking pastBooking2 = createBooking(now.minusDays(5), now.minusDays(4), item1, booker2, BookingStatus.APPROVED);
        Booking currentBooking = createBooking(now.minusDays(1), now.plusDays(1), item1, booker1, BookingStatus.APPROVED);
        Booking otherItemBooking = createBooking(now.minusDays(2), now.minusDays(1), item2, booker1, BookingStatus.APPROVED);

        entityManager.persist(pastBooking1);
        entityManager.persist(pastBooking2);
        entityManager.persist(currentBooking);
        entityManager.persist(otherItemBooking);
        entityManager.flush();
        entityManager.clear();

        List<Booking> pastBookings = bookingRepository.findPastBookingsForItem(item1.getId());

        assertThat(pastBookings).hasSize(2);
        assertThat(pastBookings).allMatch(b -> b.getItem().getId().equals(item1.getId()));
        assertThat(pastBookings).allMatch(b -> b.getEnd().isBefore(now));
        assertThat(pastBookings.get(0).getEnd()).isAfter(pastBookings.get(1).getEnd());
    }

    @Test
    void shouldFindFutureBookingsForItem() {
        LocalDateTime now = LocalDateTime.now();

        Booking futureBooking1 = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking futureBooking2 = createBooking(now.plusDays(3), now.plusDays(4), item1, booker2, BookingStatus.WAITING);
        Booking currentBooking = createBooking(now.minusDays(1), now.plusDays(1), item1, booker1, BookingStatus.APPROVED);
        Booking pastBooking = createBooking(now.minusDays(3), now.minusDays(2), item1, booker1, BookingStatus.APPROVED);

        entityManager.persist(futureBooking1);
        entityManager.persist(futureBooking2);
        entityManager.persist(currentBooking);
        entityManager.persist(pastBooking);
        entityManager.flush();
        entityManager.clear();

        List<Booking> futureBookings = bookingRepository.findFutureBookingsForItem(item1.getId());

        assertThat(futureBookings).hasSize(2);
        assertThat(futureBookings).allMatch(b -> b.getItem().getId().equals(item1.getId()));
        assertThat(futureBookings).allMatch(b -> b.getStart().isAfter(now));
        assertThat(futureBookings.get(0).getStart()).isBefore(futureBookings.get(1).getStart());
    }

    @Test
    void shouldFindPastBookingsByItemIds() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = List.of(item1.getId(), item2.getId());

        Booking past1 = createBooking(now.minusDays(3), now.minusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking past2 = createBooking(now.minusDays(5), now.minusDays(4), item1, booker2, BookingStatus.APPROVED);
        Booking past3 = createBooking(now.minusDays(2), now.minusDays(1), item2, booker1, BookingStatus.APPROVED);
        Booking current = createBooking(now.minusDays(1), now.plusDays(1), item1, booker1, BookingStatus.APPROVED);

        entityManager.persist(past1);
        entityManager.persist(past2);
        entityManager.persist(past3);
        entityManager.persist(current);
        entityManager.flush();
        entityManager.clear();

        List<Booking> pastBookings = bookingRepository.findPastBookingsByItemIds(itemIds);

        assertThat(pastBookings).hasSize(3);
        assertThat(pastBookings)
                .extracting(b -> b.getItem().getId())
                .containsExactlyInAnyOrder(item1.getId(), item1.getId(), item2.getId());
    }

    @Test
    void shouldFindFutureBookingsByItemIds() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = List.of(item1.getId(), item2.getId());

        Booking future1 = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking future2 = createBooking(now.plusDays(3), now.plusDays(4), item1, booker2, BookingStatus.WAITING);
        Booking future3 = createBooking(now.plusDays(2), now.plusDays(3), item2, booker1, BookingStatus.APPROVED);
        Booking past = createBooking(now.minusDays(3), now.minusDays(2), item1, booker1, BookingStatus.APPROVED);

        entityManager.persist(future1);
        entityManager.persist(future2);
        entityManager.persist(future3);
        entityManager.persist(past);
        entityManager.flush();
        entityManager.clear();

        List<Booking> futureBookings = bookingRepository.findFutureBookingsByItemIds(itemIds);

        assertThat(futureBookings).hasSize(3);
        assertThat(futureBookings)
                .extracting(b -> b.getItem().getId())
                .containsExactlyInAnyOrder(item1.getId(), item1.getId(), item2.getId());

        assertThat(futureBookings.get(0).getStart()).isBefore(futureBookings.get(1).getStart());
        assertThat(futureBookings.get(1).getStart()).isBefore(futureBookings.get(2).getStart());
    }

    @Test
    void shouldCheckExistsByItemIdAndBookerIdAndEndBefore() {
        LocalDateTime now = LocalDateTime.now();

        Booking pastApproved = createBooking(now.minusDays(3), now.minusDays(2), item1, booker1, BookingStatus.APPROVED);
        Booking pastRejected = createBooking(now.minusDays(5), now.minusDays(4), item1, booker1, BookingStatus.REJECTED);
        Booking current = createBooking(now.minusDays(1), now.plusDays(1), item1, booker1, BookingStatus.APPROVED);
        Booking differentBooker = createBooking(now.minusDays(3), now.minusDays(2), item1, booker2, BookingStatus.APPROVED);

        entityManager.persist(pastApproved);
        entityManager.persist(pastRejected);
        entityManager.persist(current);
        entityManager.persist(differentBooker);
        entityManager.flush();
        entityManager.clear();

        assertThat(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(item1.getId(), booker1.getId()))
                .isTrue();

        assertThat(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(item1.getId(), booker2.getId()))
                .isTrue();

        assertThat(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(item2.getId(), booker1.getId()))
                .isFalse();
    }

    @Test
    void shouldFindAllWithRelations() {
        LocalDateTime now = LocalDateTime.now();

        Booking booking1 = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.WAITING);
        Booking booking2 = createBooking(now.plusDays(3), now.plusDays(4), item2, booker2, BookingStatus.APPROVED);

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.flush();
        entityManager.clear();

        List<Booking> allBookings = bookingRepository.findAll();

        assertThat(allBookings).hasSize(2);
    }

    @Test
    void shouldDeleteBooking() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = createBooking(now.plusDays(1), now.plusDays(2), item1, booker1, BookingStatus.WAITING);

        entityManager.persist(booking);
        entityManager.flush();

        Long bookingId = booking.getId();

        bookingRepository.deleteById(bookingId);
        entityManager.flush();

        Booking deleted = entityManager.find(Booking.class, bookingId);
        assertThat(deleted).isNull();
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item createItem(String name, String description, User owner, Boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setOwner(owner);
        item.setAvailable(available);
        return item;
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end, Item item, User booker, BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        return booking;
    }
}
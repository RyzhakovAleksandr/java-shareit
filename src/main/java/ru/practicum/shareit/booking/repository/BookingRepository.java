package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.booker.id = :bookerId")
    List<Booking> findByBookerIdWithRelations(@Param("bookerId") Long bookerId, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.item.owner.id = :ownerId")
    List<Booking> findByItemOwnerIdWithRelations(@Param("ownerId") Long ownerId, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.booker.id = :bookerId AND b.status = :status")
    List<Booking> findByBookerIdAndStatusWithRelations(@Param("bookerId") Long bookerId, @Param("status") BookingStatus status, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.item.owner.id = :ownerId AND b.status = :status")
    List<Booking> findByItemOwnerIdAndStatusWithRelations(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.booker.id = :bookerId AND b.end < :end")
    List<Booking> findByBookerIdAndEndBeforeWithRelations(@Param("bookerId") Long bookerId, @Param("end") LocalDateTime end, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.booker.id = :bookerId AND b.start > :start")
    List<Booking> findByBookerIdAndStartAfterWithRelations(@Param("bookerId") Long bookerId, @Param("start") LocalDateTime start, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.booker.id = :bookerId AND b.start < :start AND b.end > :end")
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterWithRelations(@Param("bookerId") Long bookerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.item.owner.id = :ownerId AND b.end < :end")
    List<Booking> findByItemOwnerIdAndEndBeforeWithRelations(@Param("ownerId") Long ownerId, @Param("end") LocalDateTime end, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.item.owner.id = :ownerId AND b.start > :start")
    List<Booking> findByItemOwnerIdAndStartAfterWithRelations(@Param("ownerId") Long ownerId, @Param("start") LocalDateTime start, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.item.owner.id = :ownerId AND b.start < :start AND b.end > :end")
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterWithRelations(@Param("ownerId") Long ownerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Sort sort);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker WHERE b.id = :id")
    Optional<Booking> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.booker WHERE b.item.id = :itemId AND b.end < CURRENT_TIMESTAMP ORDER BY b.end DESC")
    List<Booking> findPastBookingsForItem(@Param("itemId") Long itemId);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.booker WHERE b.item.id = :itemId AND b.start > CURRENT_TIMESTAMP ORDER BY b.start ASC")
    List<Booking> findFutureBookingsForItem(@Param("itemId") Long itemId);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.booker WHERE b.item.id IN :itemIds AND b.end < CURRENT_TIMESTAMP ORDER BY b.end DESC")
    List<Booking> findPastBookingsByItemIds(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.booker WHERE b.item.id IN :itemIds AND b.start > CURRENT_TIMESTAMP ORDER BY b.start ASC")
    List<Booking> findFutureBookingsByItemIds(@Param("itemIds") List<Long> itemIds);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.item.id = :itemId AND b.booker.id = :bookerId AND b.status = 'APPROVED' AND b.end < CURRENT_TIMESTAMP")
    boolean existsByItemIdAndBookerIdAndEndBefore(@Param("itemId") Long itemId, @Param("bookerId") Long bookerId);
}

package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(Long requesterId);

    List<ItemRequest> findAllByRequesterIdNot(Long requesterId, Pageable pageable);

    @Query("SELECT ir FROM ItemRequest ir LEFT JOIN FETCH ir.requester WHERE ir.requester.id = :requesterId ORDER BY ir.created DESC")
    List<ItemRequest> findByRequestorIdWithRequestor(Long requesterId);

    @Query("SELECT ir FROM ItemRequest ir LEFT JOIN FETCH ir.requester ORDER BY ir.created DESC")
    List<ItemRequest> findAllWithRequestor();
}
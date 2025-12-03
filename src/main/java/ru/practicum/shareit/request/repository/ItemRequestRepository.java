package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    @Query("SELECT DISTINCT ir FROM ItemRequest ir LEFT JOIN FETCH ir.requestor WHERE ir.requestor.id = :requestorId ORDER BY ir.created DESC")
    List<ItemRequest> findByRequestorIdWithRequestor(Long requestorId);

    @Query("SELECT DISTINCT ir FROM ItemRequest ir LEFT JOIN FETCH ir.requestor ORDER BY ir.created DESC")
    List<ItemRequest> findAllWithRequestor();
}
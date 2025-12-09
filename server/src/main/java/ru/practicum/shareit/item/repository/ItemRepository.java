package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.request WHERE i.owner.id = :ownerId")
    List<Item> findByOwnerIdWithRequest(Long ownerId);

    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.request WHERE " +
            "(UPPER(i.name) LIKE UPPER(CONCAT('%', ?1, '%')) OR " +
            "UPPER(i.description) LIKE UPPER(CONCAT('%', ?1, '%'))) AND " +
            "i.available = true")
    List<Item> searchWithRequest(String text);
}

package ru.practicum.shareit.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT new ru.practicum.shareit.comment.dto.CommentDto(" +
            "c.id, c.text, c.author.name, c.created) " +
            "FROM Comment c WHERE c.item.id = :itemId")
    List<CommentDto> findCommentDtoByItemId(@Param("itemId") Long itemId);

    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.author WHERE c.item.id IN :itemIds")
    List<Comment> findByItemIdsWithAuthor(@Param("itemIds") List<Long> itemIds);
}

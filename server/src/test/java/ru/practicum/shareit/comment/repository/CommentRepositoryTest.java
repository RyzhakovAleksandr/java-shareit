package ru.practicum.shareit.comment.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void shouldSaveComment() {
        User author = createUser("Author", "author@example.com");
        User owner = createUser("Owner", "owner@example.com");
        Item item = createItem("Item", "Description", owner, true);

        entityManager.persist(author);
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.flush();

        Comment comment = new Comment();
        comment.setText("Great item!");
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("Great item!");
        assertThat(saved.getAuthor()).isEqualTo(author);
        assertThat(saved.getItem()).isEqualTo(item);
    }

    @Test
    void shouldFindCommentDtoByItemId() {
        User author1 = createUser("Author1", "author1@example.com");
        User author2 = createUser("Author2", "author2@example.com");
        User owner = createUser("Owner", "owner@example.com");
        Item item1 = createItem("Item1", "Desc1", owner, true);
        Item item2 = createItem("Item2", "Desc2", owner, true);

        entityManager.persist(author1);
        entityManager.persist(author2);
        entityManager.persist(owner);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.flush();

        Comment comment1 = createComment("Good item", author1, item1, LocalDateTime.now().minusDays(2));
        Comment comment2 = createComment("Excellent!", author2, item1, LocalDateTime.now().minusDays(1));
        Comment comment3 = createComment("Not bad", author1, item2, LocalDateTime.now());

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.flush();

        List<CommentDto> comments = commentRepository.findCommentDtoByItemId(item1.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(CommentDto::getText)
                .containsExactlyInAnyOrder("Good item", "Excellent!");
        assertThat(comments).extracting(CommentDto::getAuthorName)
                .containsExactlyInAnyOrder("Author1", "Author2");
    }

    @Test
    void shouldReturnEmptyListWhenNoCommentsForItem() {
        User owner = createUser("Owner", "owner@example.com");
        Item item = createItem("Item", "Desc", owner, true);

        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.flush();

        List<CommentDto> comments = commentRepository.findCommentDtoByItemId(item.getId());

        assertThat(comments).isEmpty();
    }

    @Test
    void shouldFindByItemIdsWithAuthor() {
        User author = createUser("Author", "author@example.com");
        User owner = createUser("Owner", "owner@example.com");
        Item item1 = createItem("Item1", "Desc1", owner, true);
        Item item2 = createItem("Item2", "Desc2", owner, true);
        Item item3 = createItem("Item3", "Desc3", owner, true);

        entityManager.persist(author);
        entityManager.persist(owner);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        Comment comment1 = createComment("Comment 1", author, item1, LocalDateTime.now());
        Comment comment2 = createComment("Comment 2", author, item1, LocalDateTime.now().minusHours(1));
        Comment comment3 = createComment("Comment 3", author, item2, LocalDateTime.now().minusHours(2));

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.persist(comment3);
        entityManager.flush();

        List<Long> itemIds = List.of(item1.getId(), item2.getId());

        entityManager.clear();

        List<Comment> comments = commentRepository.findByItemIdsWithAuthor(itemIds);

        assertThat(comments).hasSize(3);

        comments.forEach(comment -> {
            assertThat(comment.getAuthor()).isNotNull();
            assertThat(comment.getAuthor().getName()).isEqualTo("Author");
        });

        assertThat(comments)
                .extracting(comment -> comment.getItem().getId())
                .containsExactlyInAnyOrder(item1.getId(), item1.getId(), item2.getId());
    }

    @Test
    void shouldFindByItemIdsWithAuthor_EmptyList() {
        User owner = createUser("Owner", "owner@example.com");
        Item item = createItem("Item", "Desc", owner, true);
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.flush();

        List<Comment> comments = commentRepository.findByItemIdsWithAuthor(List.of(item.getId()));

        assertThat(comments).isEmpty();
    }

    @Test
    void shouldFindByItemIdsWithAuthor_MultipleItems() {
        User author1 = createUser("Author1", "author1@example.com");
        User author2 = createUser("Author2", "author2@example.com");
        User owner = createUser("Owner", "owner@example.com");

        Item item1 = createItem("Item1", "Desc1", owner, true);
        Item item2 = createItem("Item2", "Desc2", owner, true);
        Item item3 = createItem("Item3", "Desc3", owner, true); // No comments

        entityManager.persist(author1);
        entityManager.persist(author2);
        entityManager.persist(owner);
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
        entityManager.flush();

        for (int i = 1; i <= 3; i++) {
            Comment comment = createComment(
                    "Comment " + i,
                    i % 2 == 0 ? author1 : author2,
                    i <= 2 ? item1 : item2,
                    LocalDateTime.now().minusHours(i)
            );
            entityManager.persist(comment);
        }
        entityManager.flush();

        List<Long> allItemIds = List.of(item1.getId(), item2.getId(), item3.getId());

        entityManager.clear();

        List<Comment> comments = commentRepository.findByItemIdsWithAuthor(allItemIds);

        assertThat(comments).hasSize(3);

        assertThat(comments)
                .extracting(comment -> comment.getItem().getId())
                .containsExactlyInAnyOrder(item1.getId(), item1.getId(), item2.getId());

        assertThat(comments)
                .noneMatch(comment -> comment.getItem().getId().equals(item3.getId()));
    }

    @Test
    void shouldFindAllComments() {
        User author = createUser("Author", "author@example.com");
        User owner = createUser("Owner", "owner@example.com");
        Item item = createItem("Item", "Desc", owner, true);

        entityManager.persist(author);
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.flush();

        Comment comment1 = createComment("First", author, item, LocalDateTime.now());
        Comment comment2 = createComment("Second", author, item, LocalDateTime.now().minusHours(1));

        entityManager.persist(comment1);
        entityManager.persist(comment2);
        entityManager.flush();

        List<Comment> allComments = commentRepository.findAll();

        assertThat(allComments).hasSize(2);
        assertThat(allComments).extracting(Comment::getText)
                .containsExactlyInAnyOrder("First", "Second");
    }

    @Test
    void shouldDeleteComment() {
        User author = createUser("Author", "author@example.com");
        User owner = createUser("Owner", "owner@example.com");
        Item item = createItem("Item", "Desc", owner, true);

        entityManager.persist(author);
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.flush();

        Comment comment = createComment("To delete", author, item, LocalDateTime.now());
        entityManager.persist(comment);
        entityManager.flush();

        Long commentId = comment.getId();

        commentRepository.deleteById(commentId);
        entityManager.flush();

        Comment deleted = entityManager.find(Comment.class, commentId);
        assertThat(deleted).isNull();
    }

    @Test
    void shouldFindById() {
        User author = createUser("Author", "author@example.com");
        User owner = createUser("Owner", "owner@example.com");
        Item item = createItem("Item", "Desc", owner, true);

        entityManager.persist(author);
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.flush();

        Comment comment = createComment("Test", author, item, LocalDateTime.now());
        entityManager.persist(comment);
        entityManager.flush();

        var found = commentRepository.findById(comment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("Test");
        assertThat(found.get().getAuthor().getName()).isEqualTo("Author");
    }

    @Test
    void shouldHandleCommentDtoProjectionCorrectly() {
        User author = createUser("John Doe", "john@example.com");
        User owner = createUser("Owner", "owner@example.com");
        Item item = createItem("Drill", "Powerful drill", owner, true);

        entityManager.persist(author);
        entityManager.persist(owner);
        entityManager.persist(item);
        entityManager.flush();

        LocalDateTime createdTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        Comment comment = createComment("Very useful tool!", author, item, createdTime);
        entityManager.persist(comment);
        entityManager.flush();

        List<CommentDto> commentDtos = commentRepository.findCommentDtoByItemId(item.getId());

        assertThat(commentDtos).hasSize(1);
        CommentDto dto = commentDtos.getFirst();

        assertThat(dto.getId()).isEqualTo(comment.getId());
        assertThat(dto.getText()).isEqualTo("Very useful tool!");
        assertThat(dto.getAuthorName()).isEqualTo("John Doe");
        assertThat(dto.getCreated()).isEqualTo(createdTime);
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

    private Comment createComment(String text, User author, Item item, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(created);
        return comment;
    }
}
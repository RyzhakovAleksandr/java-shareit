package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    private ItemDto itemDto;
    private CommentCreateDto commentCreateDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);

        commentCreateDto = new CommentCreateDto("Отличная дрель!");
    }

    @Test
    void testCreateItemWhenValid() throws Exception {
        when(itemClient.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateItemWhenNameIsBlank() throws Exception {
        ItemDto invalidItemDto = new ItemDto();
        invalidItemDto.setName("");
        invalidItemDto.setDescription("Описание");
        invalidItemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateItemWhenDescriptionIsBlank() throws Exception {
        ItemDto invalidItemDto = new ItemDto();
        invalidItemDto.setName("Название");
        invalidItemDto.setDescription("");
        invalidItemDto.setAvailable(true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateItemWhenMissingUserIdHeader() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateItemWhenValid() throws Exception {
        when(itemClient.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItemByIdWhenValid() throws Exception {
        when(itemClient.getItem(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(itemDto, HttpStatus.OK));

        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItemsByOwnerWhenValid() throws Exception {
        when(itemClient.getUserItems(anyLong()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchItemsWhenValid() throws Exception {
        when(itemClient.searchItems(anyString()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void testAddCommentWhenValid() throws Exception {
        when(itemClient.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testAddCommentWhenTextIsBlank() throws Exception {
        CommentCreateDto invalidComment = new CommentCreateDto("");

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());
    }
}
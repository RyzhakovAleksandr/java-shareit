package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private ItemRequestCreateDto requestCreateDto;

    @BeforeEach
    void setUp() {
        requestCreateDto = new ItemRequestCreateDto("Нужна дрель для ремонта");
    }

    @Test
    void testCreateRequestWhenValid() throws Exception {
        when(itemRequestClient.createRequest(anyLong(), any(ItemRequestCreateDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateRequestWhenDescriptionIsBlank() throws Exception {
        ItemRequestCreateDto invalidRequest = new ItemRequestCreateDto("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Описание запроса не может быть пустым"));
    }

    @Test
    void testCreateRequestWhenMissingUserIdHeader() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRequestByIdWhenValid() throws Exception {
        when(itemRequestClient.getRequestById(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserRequestsWhenValid() throws Exception {
        when(itemRequestClient.getUserRequests(anyLong()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllRequestsWhenValidParameters() throws Exception {
        when(itemRequestClient.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllRequestsWhenFromIsNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllRequestsWhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllRequestsWhenSizeIsNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "-1"))
                .andExpect(status().isBadRequest());
    }
}
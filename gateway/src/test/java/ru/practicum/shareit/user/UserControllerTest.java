package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "Иван Иванов", "ivan@example.com");
    }

    @Test
    void testCreateUserWhenValid() throws Exception {
        when(userClient.createUser(any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(userDto, HttpStatus.OK));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUserWhenEmailInvalid() throws Exception {
        UserDto invalidUserDto = new UserDto();
        invalidUserDto.setName("Иван Иванов");
        invalidUserDto.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некорректный формат email"));
    }

    @Test
    void testCreateUserWhenNameIsBlank() throws Exception {
        UserDto invalidUserDto = new UserDto();
        invalidUserDto.setName("");
        invalidUserDto.setEmail("ivan@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Имя не может быть пустым"));
    }

    @Test
    void testCreateUserWhenEmailIsBlank() throws Exception {
        UserDto invalidUserDto = new UserDto();
        invalidUserDto.setName("Иван Иванов");
        invalidUserDto.setEmail("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email не может быть пустым"));
    }

    @Test
    void testUpdateUserWhenValid() throws Exception {
        when(userClient.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(userDto, HttpStatus.OK));

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserByIdWhenValid() throws Exception {
        when(userClient.getUser(anyLong()))
                .thenReturn(new ResponseEntity<>(userDto, HttpStatus.OK));

        mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllUsersWhenValid() throws Exception {
        when(userClient.getAllUsers())
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteUserWhenValid() throws Exception {
        when(userClient.deleteUser(anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isOk());
    }
}
package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    @Test
    void shouldCreateUserDtoWithNoArgsConstructor() {
        UserDto userDto = new UserDto();

        assertNotNull(userDto);
        assertNull(userDto.getId());
        assertNull(userDto.getName());
        assertNull(userDto.getEmail());
    }

    @Test
    void shouldCreateUserDtoWithAllArgsConstructor() {
        Long id = 1L;
        String name = "John Doe";
        String email = "john@example.com";

        UserDto userDto = new UserDto(id, name, email);

        assertNotNull(userDto);
        assertEquals(id, userDto.getId());
        assertEquals(name, userDto.getName());
        assertEquals(email, userDto.getEmail());
    }

    @Test
    void shouldSetAndGetFields() {
        UserDto userDto = new UserDto();
        Long id = 2L;
        String name = "Jane Smith";
        String email = "jane@example.com";

        userDto.setId(id);
        userDto.setName(name);
        userDto.setEmail(email);

        assertEquals(id, userDto.getId());
        assertEquals(name, userDto.getName());
        assertEquals(email, userDto.getEmail());
    }

    @Test
    void shouldReturnCorrectStringRepresentation() {
        UserDto userDto = new UserDto(3L, "Test User", "test@example.com");

        String stringRepresentation = userDto.toString();

        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("Test User"));
        assertTrue(stringRepresentation.contains("test@example.com"));
        assertTrue(stringRepresentation.contains("id=3"));
    }

    @Test
    void testEqualsAndHashCode() {
        UserDto userDto1 = new UserDto(1L, "User", "user@example.com");
        UserDto userDto2 = new UserDto(1L, "User", "user@example.com");
        UserDto userDto3 = new UserDto(2L, "Another User", "another@example.com");

        assertEquals(userDto1, userDto2);
        assertNotEquals(userDto1, userDto3);
        assertEquals(userDto1.hashCode(), userDto2.hashCode());
        assertNotEquals(userDto1.hashCode(), userDto3.hashCode());
    }

    @Test
    void shouldHaveAllArgsConstructorFieldsInCorrectOrder() {
        UserDto userDto = new UserDto(1L, "Test", "test@example.com");

        assertEquals(1L, userDto.getId());
        assertEquals("Test", userDto.getName());
        assertEquals("test@example.com", userDto.getEmail());
    }

    @Test
    void shouldHandleNullValues() {
        UserDto userDto = new UserDto(null, null, null);

        assertNull(userDto.getId());
        assertNull(userDto.getName());
        assertNull(userDto.getEmail());
    }

    @Test
    void shouldBeAbleToModifyFields() {
        UserDto userDto = new UserDto(1L, "Original", "original@example.com");

        userDto.setId(2L);
        userDto.setName("Modified");
        userDto.setEmail("modified@example.com");

        assertEquals(2L, userDto.getId());
        assertEquals("Modified", userDto.getName());
        assertEquals("modified@example.com", userDto.getEmail());
    }
}
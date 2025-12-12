package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ActiveProfiles("test")
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();


    @Test
    void testSerializeUserDto() throws Exception {
        UserDto userDto = new UserDto(1L, "Иван Иванов", "ivan@example.com");

        String json = objectMapper.writeValueAsString(userDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Иван Иванов\"");
        assertThat(json).contains("\"email\":\"ivan@example.com\"");
    }

    @Test
    void testDeserializeUserDto() throws Exception {
        String json = "{\"id\":1,\"name\":\"Иван Иванов\",\"email\":\"ivan@example.com\"}";

        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("Иван Иванов");
        assertThat(userDto.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void testValidationWhenEmailIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван Иванов");
        userDto.setEmail("invalid-email");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Некорректный формат email");
    }

    @Test
    void testValidationWhenEmailIsBlank() {
        UserDto userDto = new UserDto();
        userDto.setName("Иван Иванов");
        userDto.setEmail("");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Email не может быть пустым");
    }

    @Test
    void testValidationWhenNameIsBlank() {

        UserDto userDto = new UserDto();
        userDto.setName("");
        userDto.setEmail("ivan@example.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Имя не может быть пустым");
    }

    @Test
    void testValidationWhenAllFieldsValid() {

        UserDto userDto = new UserDto();
        userDto.setName("Иван Иванов");
        userDto.setEmail("ivan@example.com");

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).isEmpty();
    }
}
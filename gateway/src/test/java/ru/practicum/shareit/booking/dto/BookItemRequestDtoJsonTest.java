package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@ActiveProfiles("test")
class BookItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private Validator validator;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSerializeBookItemRequestDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"start\":\"" + start.format(formatter) + "\"");
        assertThat(json).contains("\"end\":\"" + end.format(formatter) + "\"");
    }

    @Test
    void testDeserializeBookItemRequestDto() throws Exception {
        String startStr = "2024-12-25T10:00:00";
        String endStr = "2024-12-26T10:00:00";
        String json = String.format("{\"itemId\":1,\"start\":\"%s\",\"end\":\"%s\"}", startStr, endStr);

        BookItemRequestDto dto = objectMapper.readValue(json, BookItemRequestDto.class);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.parse(startStr));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.parse(endStr));
    }

    @Test
    void testValidationWhenStartInPast() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
    }

    @Test
    void testValidationWhenEndNotFuture() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now();
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
    }

    @Test
    void testValidationWhenAllFieldsValid() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
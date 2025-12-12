package ru.practicum.shareit.item.dto;

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
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final Validator validator;

    {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testSerializeItemDto() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);
        itemDto.setRequestId(100L);

        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"description\":\"Аккумуляторная дрель\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":100");
    }

    @Test
    void testDeserializeItemDto() throws Exception {
        String json = "{\"id\":1,\"name\":\"Дрель\",\"description\":\"Аккумуляторная дрель\",\"available\":true,\"requestId\":100}";

        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);

        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Аккумуляторная дрель");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(100L);
    }

    @Test
    void testValidationWhenNameIsBlank() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("");
        itemDto.setDescription("Описание");
        itemDto.setAvailable(true);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Название не может быть пустым");
    }

    @Test
    void testValidationWhenDescriptionIsBlank() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Название");
        itemDto.setDescription("");
        itemDto.setAvailable(true);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание не может быть пустым");
    }

    @Test
    void testValidationWhenAllFieldsValid() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("Аккумуляторная дрель");
        itemDto.setAvailable(true);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void testValidationWhenNameIsNull() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(null);
        itemDto.setDescription("Описание");
        itemDto.setAvailable(true);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Название не может быть пустым");
    }

    @Test
    void testValidationWhenDescriptionIsNull() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Название");
        itemDto.setDescription(null);
        itemDto.setAvailable(true);

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Описание не может быть пустым");
    }
}
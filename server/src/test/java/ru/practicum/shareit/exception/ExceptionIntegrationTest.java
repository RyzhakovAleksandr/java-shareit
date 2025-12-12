package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для исключений")
class ExceptionIntegrationTest {

    @Test
    @DisplayName("Проверка иерархии исключений и их сообщений")
    void exceptionHierarchy_shouldWorkCorrectly() {
        RuntimeException[] exceptions = {
                new AccessDeniedException("Access denied"),
                new UserNotFoundException("User not found"),
                new DuplicateEmailException("Duplicate email"),
                new ValidationException("Validation failed"),
                new ItemNotFoundException("Item not found")
        };

        for (RuntimeException exception : exceptions) {
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().length() > 0);
            assertTrue(exception instanceof RuntimeException);
        }
    }

    @Test
    @DisplayName("ErrorResponse с разными сообщениями")
    void errorResponse_withDifferentMessages() {
        String[] messages = {
                "Short",
                "Very long error message with details about what went wrong",
                "",
                "Special characters: !@#$%^&*()",
                "Multi\nline\nerror"
        };

        for (String message : messages) {
            ErrorResponse response = new ErrorResponse(message);
            assertEquals(message, response.getError());
        }
    }
}
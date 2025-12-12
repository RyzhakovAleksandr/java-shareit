package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для классов исключений")
class ExceptionsTest {

    @Test
    @DisplayName("AccessDeniedException создается с правильным сообщением")
    void accessDeniedException_shouldHaveCorrectMessage() {
        String expectedMessage = "Доступ запрещен";
        AccessDeniedException exception = new AccessDeniedException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("AccessDeniedException наследуется от RuntimeException")
    void accessDeniedException_shouldBeRuntimeException() {
        AccessDeniedException exception = new AccessDeniedException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("UserNotFoundException создается с правильным сообщением")
    void userNotFoundException_shouldHaveCorrectMessage() {
        String expectedMessage = "Пользователь не найден";
        UserNotFoundException exception = new UserNotFoundException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("UserNotFoundException наследуется от RuntimeException")
    void userNotFoundException_shouldBeRuntimeException() {
        UserNotFoundException exception = new UserNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("DuplicateEmailException создается с правильным сообщением")
    void duplicateEmailException_shouldHaveCorrectMessage() {
        String expectedMessage = "Email уже используется";
        DuplicateEmailException exception = new DuplicateEmailException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("DuplicateEmailException наследуется от RuntimeException")
    void duplicateEmailException_shouldBeRuntimeException() {
        DuplicateEmailException exception = new DuplicateEmailException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("ValidationException создается с правильным сообщением")
    void validationException_shouldHaveCorrectMessage() {
        String expectedMessage = "Ошибка валидации";
        ValidationException exception = new ValidationException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("ValidationException наследуется от RuntimeException")
    void validationException_shouldBeRuntimeException() {
        ValidationException exception = new ValidationException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("ItemNotFoundException создается с правильным сообщением")
    void itemNotFoundException_shouldHaveCorrectMessage() {
        String expectedMessage = "Вещь не найдена";
        ItemNotFoundException exception = new ItemNotFoundException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("ItemNotFoundException наследуется от RuntimeException")
    void itemNotFoundException_shouldBeRuntimeException() {
        ItemNotFoundException exception = new ItemNotFoundException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("ErrorResponse создается с правильным сообщением")
    void errorResponse_shouldHaveCorrectMessage() {
        String expectedError = "Сообщение об ошибке";
        ErrorResponse errorResponse = new ErrorResponse(expectedError);
        assertEquals(expectedError, errorResponse.getError());
    }

    @Test
    @DisplayName("ErrorResponse имеет геттер для поля error")
    void errorResponse_shouldHaveGetter() {
        String errorMessage = "Test error";
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        String actualError = errorResponse.getError();
        assertEquals(errorMessage, actualError);
    }
}
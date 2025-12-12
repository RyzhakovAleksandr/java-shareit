package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты для ErrorHandler")
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Test
    @DisplayName("Обработка IllegalArgumentException")
    void handleIllegalArgumentException() {
        String errorMessage = "Некорректный аргумент";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);
        ErrorResponse response = errorHandler.handleNotFound(exception);
        assertNotNull(response);
        assertEquals(errorMessage, response.getError());
    }

    @Test
    @DisplayName("Обработка UserNotFoundException")
    void handleUserNotFoundException() {
        String errorMessage = "Пользователь не найден";
        UserNotFoundException exception = new UserNotFoundException(errorMessage);
        ErrorResponse response = errorHandler.handleNotFound(exception);
        assertNotNull(response);
        assertEquals(errorMessage, response.getError());
    }

    @Test
    @DisplayName("Обработка ItemNotFoundException")
    void handleItemNotFoundException() {
        String errorMessage = "Вещь не найдена";
        ItemNotFoundException exception = new ItemNotFoundException(errorMessage);
        ErrorResponse response = errorHandler.handleNotFound(exception);
        assertNotNull(response);
        assertEquals(errorMessage, response.getError());
    }

    @Test
    @DisplayName("Обработка ValidationException")
    void handleValidationException() {
        String errorMessage = "Ошибка валидации";
        ValidationException exception = new ValidationException(errorMessage);
        ErrorResponse response = errorHandler.handleValidationException(exception);
        assertNotNull(response);
        assertEquals(errorMessage, response.getError());
    }

    @Test
    @DisplayName("Обработка AccessDeniedException")
    void handleAccessDeniedException() {
        String errorMessage = "Доступ запрещен";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);
        ErrorResponse response = errorHandler.handleAccessDenied(exception);
        assertNotNull(response);
        assertEquals(errorMessage, response.getError());
    }

    @Test
    @DisplayName("Обработка MethodArgumentNotValidException с несколькими ошибками")
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = Arrays.asList(
                new FieldError("object", "field1", "Ошибка в поле 1"),
                new FieldError("object", "field2", "Ошибка в поле 2")
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        Map<String, String> errors = errorHandler.handleValidationExceptions(exception);

        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Ошибка в поле 1", errors.get("field1"));
        assertEquals("Ошибка в поле 2", errors.get("field2"));
    }

    @Test
    @DisplayName("Обработка MethodArgumentNotValidException без ошибок")
    void handleMethodArgumentNotValidException_withNoErrors_shouldReturnEmptyMap() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        Map<String, String> errors = errorHandler.handleValidationExceptions(exception);
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Обработка общего Exception")
    void handleGenericException_shouldReturnInternalServerErrorResponse() {
        String errorMessage = "Что-то пошло не так";
        Exception exception = new Exception(errorMessage);
        ErrorResponse response = errorHandler.handleException(exception);
        assertNotNull(response);
        assertTrue(response.getError().contains("Внутренняя ошибка сервера"));
        assertTrue(response.getError().contains(errorMessage));
    }

    @Test
    @DisplayName("Обработка RuntimeException")
    void handleRuntimeException_shouldReturnInternalServerErrorResponse() {
        String errorMessage = "Runtime ошибка";
        RuntimeException exception = new RuntimeException(errorMessage);
        ErrorResponse response = errorHandler.handleException(exception);
        assertNotNull(response);
        assertTrue(response.getError().contains("Внутренняя ошибка сервера"));
        assertTrue(response.getError().contains(errorMessage));
    }

    @Test
    @DisplayName("Проверка аннотаций в ErrorHandler")
    void errorHandler_shouldHaveCorrectAnnotations() throws NoSuchMethodException {
        Class<?> errorHandlerClass = ErrorHandler.class;
        assertTrue(errorHandlerClass.isAnnotationPresent(org.springframework.web.bind.annotation.RestControllerAdvice.class),
                "ErrorHandler должен быть аннотирован @RestControllerAdvice");
        Method handleNotFoundMethod = errorHandlerClass.getDeclaredMethod("handleNotFound", RuntimeException.class);
        assertTrue(handleNotFoundMethod.isAnnotationPresent(org.springframework.web.bind.annotation.ExceptionHandler.class));
        assertTrue(handleNotFoundMethod.isAnnotationPresent(ResponseStatus.class));
        assertEquals(HttpStatus.NOT_FOUND, handleNotFoundMethod.getAnnotation(ResponseStatus.class).value());

        Method handleValidationExceptionMethod = errorHandlerClass.getDeclaredMethod("handleValidationException", ValidationException.class);
        assertTrue(handleValidationExceptionMethod.isAnnotationPresent(ResponseStatus.class));
        assertEquals(HttpStatus.BAD_REQUEST, handleValidationExceptionMethod.getAnnotation(ResponseStatus.class).value());

        Method handleAccessDeniedMethod = errorHandlerClass.getDeclaredMethod("handleAccessDenied", AccessDeniedException.class);
        assertTrue(handleAccessDeniedMethod.isAnnotationPresent(ResponseStatus.class));
        assertEquals(HttpStatus.FORBIDDEN, handleAccessDeniedMethod.getAnnotation(ResponseStatus.class).value());
    }
}
package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты на наследование исключений")
class ExceptionInheritanceTest {

    @Test
    @DisplayName("Все пользовательские исключения наследуются от RuntimeException")
    void allCustomExceptions_shouldExtendRuntimeException() {
        RuntimeException[] exceptions = {
                new AccessDeniedException("test"),
                new UserNotFoundException("test"),
                new DuplicateEmailException("test"),
                new ValidationException("test"),
                new ItemNotFoundException("test")
        };

        for (RuntimeException exception : exceptions) {
            assertTrue(exception instanceof RuntimeException,
                    exception.getClass().getSimpleName() + " должен наследоваться от RuntimeException");
        }
    }

    @Test
    @DisplayName("Исключения не должны иметь общий суперкласс кроме RuntimeException")
    void exceptions_shouldNotHaveCommonCustomSuperclass() {
        Class<?>[] exceptionClasses = {
                AccessDeniedException.class,
                UserNotFoundException.class,
                DuplicateEmailException.class,
                ValidationException.class,
                ItemNotFoundException.class
        };

        for (Class<?> exceptionClass : exceptionClasses) {
            assertEquals(RuntimeException.class, exceptionClass.getSuperclass(),
                    exceptionClass.getSimpleName() + " должен напрямую наследоваться от RuntimeException");
        }
    }
}
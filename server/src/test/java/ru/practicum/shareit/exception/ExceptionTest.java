package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void userNotFoundException() {
        String message = "Пользователь не найден";
        UserNotFoundException exception = new UserNotFoundException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void itemNotFoundException() {
        String message = "Вещь не найдена";
        ItemNotFoundException exception = new ItemNotFoundException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void validationException() {
        String message = "Ошибка валидации";
        ValidationException exception = new ValidationException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void accessDeniedException() {
        String message = "Доступ запрещен";
        AccessDeniedException exception = new AccessDeniedException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void duplicateEmailException() {
        String message = "Email уже используется";
        DuplicateEmailException exception = new DuplicateEmailException(message);
        assertThat(exception.getMessage()).isEqualTo(message);
    }
}
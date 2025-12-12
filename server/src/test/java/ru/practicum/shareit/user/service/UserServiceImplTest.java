package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
    }

    @Test
    void createUserWhenValidData() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWhenDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("Пользователь с email test@example.com уже существует");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUserWhenInvalidEmail() {
        userDto.setEmail("invalid-email");

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректный формат email");

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserByIdWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByIdWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");

        verify(userRepository).findById(999L);
    }

    @Test
    void updateUserWhenValidUpdate() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("new@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserWhenEmailSameAsExisting() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("same@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("same@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("New Name");
        updatedUser.setEmail("same@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("same@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString()); // Should not check duplicate for same email
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAllUsersWhenUsersExist() {
        User user1 = new User(1L, "User1", "user1@example.com");
        User user2 = new User(2L, "User2", "user2@example.com");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getName)
                .containsExactly("User1", "User2");

        verify(userRepository).findAll();
    }

    @Test
    void deleteUserWhenCalled() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}
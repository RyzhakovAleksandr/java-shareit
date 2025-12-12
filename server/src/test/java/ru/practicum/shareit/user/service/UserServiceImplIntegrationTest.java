package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setName("Test User");
        testUserDto.setEmail("test@example.com");
    }

    @Test
    void createUserWhenValidData() {
        UserDto createdUser = userService.createUser(testUserDto);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Test User");
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");

        User savedUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assertThat(savedUser.getName()).isEqualTo("Test User");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void createUserWhenDuplicateEmail() {
        userService.createUser(testUserDto);

        UserDto duplicateUserDto = new UserDto();
        duplicateUserDto.setName("Another User");
        duplicateUserDto.setEmail("test@example.com");

        assertThatThrownBy(() -> userService.createUser(duplicateUserDto))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("Пользователь с email test@example.com уже существует");
    }

    @Test
    void createUserWhenInvalidEmail() {
        testUserDto.setEmail("invalid-email");

        assertThatThrownBy(() -> userService.createUser(testUserDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Некорректный формат email");
    }

    @Test
    void getUserByIdWhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);

        UserDto foundUser = userService.getUserById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getName()).isEqualTo("Test User");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getUserByIdWhenUserNotExists() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");
    }

    @Test
    void updateUserWhenValidUpdate() {
        UserDto createdUser = userService.createUser(testUserDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");

        UserDto updatedUser = userService.updateUser(createdUser.getId(), updateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

        User dbUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assertThat(dbUser.getName()).isEqualTo("Updated Name");
        assertThat(dbUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateUserWhenOnlyName() {
        UserDto createdUser = userService.createUser(testUserDto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Updated Name Only");

        UserDto updatedUser = userService.updateUser(createdUser.getId(), updateDto);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("Updated Name Only");
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com"); // Email should remain unchanged
    }

    @Test
    void getAllUsersWhenUsersExist() {
        userService.createUser(testUserDto);

        UserDto anotherUserDto = new UserDto();
        anotherUserDto.setName("Another User");
        anotherUserDto.setEmail("another@example.com");
        userService.createUser(anotherUserDto);

        var users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("Test User", "Another User");
    }

    @Test
    void deleteUserWhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);

        userService.deleteUser(createdUser.getId());

        assertThat(userRepository.existsById(createdUser.getId())).isFalse();
    }

    @Test
    void deleteUserWhenUserNotExists() {
        userService.deleteUser(999L);
    }
}
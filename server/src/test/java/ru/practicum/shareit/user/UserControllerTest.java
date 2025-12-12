package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser() {
        UserDto request = new UserDto();
        UserDto expected = new UserDto();
        when(userService.createUser(request)).thenReturn(expected);

        UserDto result = userController.createUser(request);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getAllUsers() {
        List<UserDto> expected = Collections.singletonList(new UserDto());
        when(userService.getAllUsers()).thenReturn(expected);

        List<UserDto> result = userController.getAllUsers();

        assertThat(result).isEqualTo(expected);
    }
}
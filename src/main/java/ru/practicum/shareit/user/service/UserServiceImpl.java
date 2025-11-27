package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (!isValidEmail(userDto.getEmail())) {
            throw new ValidationException("Некорректный формат email");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException(String.format("Пользователь с email %s уже существует", userDto.getEmail()));
        }

        User user = UserMapper.toUser(userDto);
        User saveUser = userRepository.save(user);
        return UserMapper.toUserDto(saveUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с ID %d не найден", userId)));

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (!isValidEmail(userDto.getEmail())) {
                throw new ValidationException("Некорректный формат email");
            }
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new DuplicateEmailException(String.format("Пользователь с email %s уже существует", userDto.getEmail()));
            }
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь с ID %d не найден", id)));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private boolean isValidEmail(String email) {
        return email != null &&
                !email.isBlank() &&
                email.contains("@") &&
                email.indexOf("@") < email.lastIndexOf(".");
    }
}

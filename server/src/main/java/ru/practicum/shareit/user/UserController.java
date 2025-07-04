package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        UserDto createdUserDto = userService.createUser(userDto);
        log.info("Добавлен новый пользователь: {}", createdUserDto);
        return createdUserDto;
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable int userId,
                              @RequestBody UserDto userDto) {
        UserDto updatingUserDto = userService.updateUser(userId, userDto);
        log.info("Обновлен пользователь: {}", updatingUserDto);
        return updatingUserDto;
    }

    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable int userId) {
        UserDto gettingUserDto = userService.getUserById(userId);
        log.info("Получен пользователь: {}", gettingUserDto);
        return gettingUserDto;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        List<UserDto> allUsersDto = userService.getUsers();
        log.info("Список всех пользователей: {}", allUsersDto);
        return allUsersDto;
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable int userId) {
        userService.deleteUser(userId);
        log.info("Удален пользователь с id: {}", userId);
    }
}
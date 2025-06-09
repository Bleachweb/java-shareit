package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    UserDto updateUser(int userId, UserDto userDto);

    UserDto getUserById(int userId);

    List<UserDto> getUsers();

    void deleteUser(int userId);
}
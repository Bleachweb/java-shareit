package ru.practicum.shareit.user;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface UserService {

    @Transactional
    UserDto createUser(UserDto userDto);

    @Transactional
    UserDto updateUser(int userId, UserDto userDto);

    UserDto getUserById(int userId);

    List<UserDto> getUsers();

    @Transactional
    void deleteUser(int userId);
}
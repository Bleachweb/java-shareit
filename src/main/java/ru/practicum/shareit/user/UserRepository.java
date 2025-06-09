package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User addUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(int userId);

    List<User> getUsers();

    void deleteUser(int userId);
}
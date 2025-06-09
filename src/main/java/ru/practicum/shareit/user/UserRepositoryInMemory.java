package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DataDuplicationException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.*;

@Slf4j
@Repository
public class UserRepositoryInMemory implements UserRepository {

    private Integer userId = 0;
    private final Map<Integer, User> users = new HashMap<>();
    private final Set<String> usersEmail = new HashSet<>();

    @Override
    public User addUser(User user) {
        final String email = user.getEmail();
        if (usersEmail.contains(email)) {
            log.warn("Пользователь с email {} уже существует", email);
            throw new DataDuplicationException("Пользователь с email " + email + " уже существует");
        }
        int id = getId();
        user.setId(id);
        users.put(id, user);
        usersEmail.add(email);
        return user;
    }

    @Override
    public User updateUser(User user) {
        final String email = user.getEmail();
        users.computeIfPresent(user.getId(), (id, u) -> {
            if (!email.equals(u.getEmail())) {
                if (usersEmail.contains(email)) {
                    log.warn("Невозможно изменить email на {}, так как он уже существует", email);
                    throw new DataDuplicationException("Невозможно изменить email на " + email + ", так как он уже существует");
                }
                usersEmail.remove(u.getEmail());
                usersEmail.add(email);
            }
            return user;
        });
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUserById(int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> getUsers() {
        return users.values().stream().toList();
    }

    @Override
    public void deleteUser(int userId) {
        User deletedUser = getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        users.remove(userId);
        usersEmail.remove(deletedUser.getEmail());
    }

    private Integer getId() {
        userId++;
        return userId;
    }
}
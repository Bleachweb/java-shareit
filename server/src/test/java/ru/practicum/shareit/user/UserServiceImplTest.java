package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.DataDuplicationException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {

    private final EntityManager em;
    private final UserService service;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1, "Ivan Ivanov", "ivan.ivanov@ya.ru");
    }

    @Test
    void createUser_whenUserEmailValid_thenSavedUser() {
        UserDto savedUser = service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User AS u where u.id = :userId", User.class);
        User createdUser = query.setParameter("userId", savedUser.getId()).getSingleResult();

        assertThat(createdUser, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(savedUser.getName())),
                hasProperty("email", equalTo(savedUser.getEmail()))));
    }

    @Test
    void createUser_whenUserEmailNotValid_thenDuplicateFoundExceptionThrown() {
        service.createUser(userDto);

        DataDuplicationException duplicateKeyException = assertThrows(DataDuplicationException.class,
                () -> service.createUser(userDto));
        assertEquals("Данный email:" + userDto.getEmail() + " уже используется",
                duplicateKeyException.getMessage());
    }

    @Test
    void updateUserEmail_whenEmailNotValid_thenDuplicateFoundExceptionThrown() {
        service.createUser(userDto);
        UserDto updatedUser = service.createUser(new UserDto(null, "Peter", "petrov@ya.ru"));
        updatedUser.setName(userDto.getName());
        service.updateUser(updatedUser.getId(), updatedUser);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User AS u where u.id = :userId", User.class);
        User userUpdated = query.setParameter("userId", updatedUser.getId()).getSingleResult();

        assertThat(userUpdated, allOf(
                hasProperty("id", equalTo(updatedUser.getId())),
                hasProperty("name", equalTo(updatedUser.getName())),
                hasProperty("email", equalTo(updatedUser.getEmail())))
        );
    }

    @Test
    void updateUserName_whenUserValid_thenUpdateUser() {
        UserDto updatedUser = new UserDto(null, "Ivan", null);
        UserDto createdUser = service.createUser(userDto);
        service.updateUser(createdUser.getId(), updatedUser);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User AS u where u.id = :userId", User.class);
        User userUpdated = query.setParameter("userId", createdUser.getId()).getSingleResult();

        assertThat(userUpdated, allOf(
                hasProperty("id", equalTo(createdUser.getId())),
                hasProperty("name", equalTo(updatedUser.getName())))
        );
    }

    @Test
    void updateUserEmail_whenUserValid_thenUpdateUser() {
        UserDto updatedUser = new UserDto(null, null, "ivanov@ya.ru");
        UserDto createdUser = service.createUser(userDto);
        service.updateUser(createdUser.getId(), updatedUser);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User AS u where u.id = :userId", User.class);
        User userUpdated = query.setParameter("userId", createdUser.getId()).getSingleResult();

        assertThat(userUpdated, allOf(
                hasProperty("id", equalTo(createdUser.getId())),
                hasProperty("email", equalTo(updatedUser.getEmail()))
        ));
    }

    @Test
    void getUserById_whenUserFound_thenReturnUser() {
        UserDto savedUser = service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("SELECT u FROM User AS u where u.id = :userId", User.class);
        User createdUser = query.setParameter("userId", savedUser.getId()).getSingleResult();

        assertThat(createdUser, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(savedUser.getName())),
                hasProperty("email", equalTo(savedUser.getEmail()))));
    }

    @Test
    void getUserById_whenUserNotFound_thenUserNotFoundExceptionThrown() {
        final int userId = userDto.getId();

        NotFoundException userNotFoundException = assertThrows(NotFoundException.class,
                () -> service.getUserById(userId));
        assertEquals("Пользователь с id - " + userId + " не найден", userNotFoundException.getMessage());
    }

    @Test
    void getAllUsers_whenInvoked_thenResponseIsOkWithUsersListInBody() {
        List<UserDto> createdUsers = List.of(
                new UserDto(2, "Пётр Петров", "peter@email.com"),
                new UserDto(3, "Иван Иванов", "ivan@email.com"),
                new UserDto(4, "Сидр Сидоров", "sidr@email.com"));

        createdUsers.forEach(service::createUser);
        List<UserDto> users = service.getUsers();

        users.forEach(userDto -> assertThat(users, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(userDto.getName())),
                hasProperty("email", equalTo(userDto.getEmail()))
        ))));
    }

    @Test
    void deleteUser_whenUserFound_thenResponseIsOk() {
        UserDto savedUser = service.createUser(userDto);
        final int userId = savedUser.getId();
        service.deleteUser(userId);

        NotFoundException userNotFoundException = assertThrows(NotFoundException.class,
                () -> service.getUserById(userId));
        assertEquals("Пользователь с id - " + userId + " не найден", userNotFoundException.getMessage());
    }
}
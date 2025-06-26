package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.Constants;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        userDto = new UserDto(
                1,
                "Ivan Ivanov",
                "ivan.ivanov@ya.ru"
        );
    }

    @Test
    void createUser_whenUserValid_thenSavedUser() throws Exception {
        when(service.createUser(any()))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .header(X_SHARER_USER_ID, userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(service, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_whenUserValid_thenUpdateUser() throws Exception {
        final int userId = userDto.getId();
        UserDto updatedUser = new UserDto(userId, "Ivan", "ivan.ivanov@ya.ru");

        when(service.updateUser(userId, updatedUser)).thenReturn(updatedUser);

        mvc.perform(patch("/users/" + userId)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(updatedUser))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId)))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));

        verify(service, times(1)).updateUser(userId, updatedUser);
    }

    @Test
    void getUserById_whenUserFound_thenReturnUser() throws Exception {
        final int userId = userDto.getId();

        when(service.getUserById(userId)).thenReturn(userDto);

        mvc.perform(get("/users/" + userId)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId)))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(service, times(1)).getUserById(userId);
    }

    @Test
    void getAllUsers_whenInvoked_thenResponseIsOkWithUsersListInBody() throws Exception {
        List<UserDto> users = List.of(userDto);

        when(service.getUsers()).thenReturn(users);

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(users)));

        verify(service, times(1)).getUsers();
    }

    @Test
    void deleteUser_whenUserFound_thenResponseIsOk() throws Exception {
        final int userId = userDto.getId();

        mvc.perform(delete("/users/" + userId)
                        .header(X_SHARER_USER_ID, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service, times(1)).deleteUser(userId);
    }
}
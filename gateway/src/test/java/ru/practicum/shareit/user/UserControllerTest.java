package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    private UserRequestDto requestDto;
    private int userId;

    @BeforeEach
    void setUp() {
        userId = 1;
        requestDto = new UserRequestDto(userId, "ivan", "ivanov@ya.ru");
    }

    @Test
    void createUser_whenUserValid_thenResponseIsOk() throws Exception {
        when(userClient.createUser(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/users")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    void createUser_whenUserNameNotValid_thenReturnBadRequest() throws Exception {
        requestDto = new UserRequestDto(userId, "", "ivanov@ya.ru");

        when(userClient.createUser(any())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/users")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any(UserRequestDto.class));
    }

    @Test
    void createUser_whenUserEmailNotValid_thenReturnBadRequest() throws Exception {
        requestDto = new UserRequestDto(userId, "ivan", "ivanov");

        when(userClient.createUser(any())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/users")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).createUser(any(UserRequestDto.class));
    }

    @Test
    void updateUser_whenUserValid_thenResponseIsOk() throws Exception {
        UserRequestDto updatedRequestDto = new UserRequestDto(userId, "ivan", "petrov@ya.ru");

        when(userClient.updateUser(userId, updatedRequestDto)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(patch("/users/{userId}", userId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(updatedRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUser_whenUserFound_thenResponseIsOk() throws Exception {
        when(userClient.getUser(userId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/users/{userId}", userId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient, times(1)).getUser(userId);
    }

    @Test
    void getUser_whenUserNotFound_thenResponseNotFound() throws Exception {
        when(userClient.getUser(userId)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mvc.perform(get("/users/{userId}", userId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userClient, times(1)).getUser(userId);
    }

    @Test
    void deleteUser_whenUserFound_thenResponseIsOk() throws Exception {
        mvc.perform(delete("/users/{userId}", userId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userClient, times(1)).deleteUser(userId);
    }
}
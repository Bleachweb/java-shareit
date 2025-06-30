package ru.practicum.shareit.request;

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
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    @Mock
    private ItemRequestService service;

    @InjectMocks
    private ItemRequestController controller;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private ItemRequestDtoRequest requestDto;
    private ItemRequestDto responseDto;

    private final String url = "/requests";
    private final int requestId = 1;
    private final int userId = 1;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        User user = new User();
        user.setId(userId);
        user.setName("Test");
        user.setEmail("test@test.com");

        requestDto = new ItemRequestDtoRequest();
        requestDto.setDescription("description");

        responseDto = new ItemRequestDto();
        responseDto.setId(requestId);
        responseDto.setDescription("description");
        responseDto.setRequester(user);
    }

    @Test
    void createRequest_whenRequestValid_thenSavedRequest() throws Exception {
        when(service.createRequest(userId, requestDto)).thenReturn(responseDto);

        mvc.perform(post(url)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())));

        verify(service, times(1)).createRequest(userId, requestDto);
    }

    @Test
    void getRequestById_whenRequestFound_thenReturnRequest() throws Exception {
        when(service.getRequestById(userId, requestId)).thenReturn(responseDto);

        mvc.perform(get(url + "/" + requestId)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(responseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(responseDto.getDescription())));

        verify(service, times(1)).getRequestById(userId, requestId);
    }

    @Test
    void getAllRequestByUserId_whenInvoked_thenResponseIsOkWithRequestsListInBody() throws Exception {
        List<ItemRequestDto> requests = List.of(responseDto);

        when(service.getAllRequestByUserId(userId)).thenReturn(requests);

        mvc.perform(get(url)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(responseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service, times(1)).getAllRequestByUserId(userId);
    }

    @Test
    void getAllRequest_whenInvoked_thenResponseIsOkWithRequestsListInBody() throws Exception {
        List<ItemRequestDto> requests = List.of(responseDto);

        when(service.getAllRequest()).thenReturn(requests);

        mvc.perform(get(url + "/all")
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(responseDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service, times(1)).getAllRequest();
    }
}
package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService service;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private BookingController controller;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private BookingDtoRequest bookingDtoRequest;
    private BookingDtoResponse bookingDtoResponse;

    private final String url = "/bookings";
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;
    private final int userId = 1;
    private final int bookingId = 1;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mapper.registerModule(new JavaTimeModule());

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusNanos(5);

        User user = new User();
        user.setId(userId);
        user.setName("UserDto");
        user.setEmail("userdto@email.com");

        UserDto userDto = UserDto.builder()
                .name("UserDto")
                .email("userdto@email.com")
                .build();

        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .name("Item")
                .description("Item")
                .available(true)
                .requestId(1)
                .build();

        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(1)
                .start(start)
                .end(end)
                .build();

        bookingDtoResponse = BookingDtoResponse.builder()
                .item(Item.builder().build())
                .start(start)
                .end(end)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        userService.createUser(userDto);
        itemService.createItem(userId, itemDtoRequest);
    }

    @Test
    void createBooking_whenBookingValid_thenSavedBooking() throws Exception {
        when(service.createBooking(userId, bookingDtoRequest)).thenReturn(bookingDtoResponse);

        mvc.perform(post(url)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(bookingDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDtoResponse)));

        verify(service, times(1)).createBooking(userId, bookingDtoRequest);
    }

    @Test
    void approvedBooking_whenBookingApproved_thenUpdateBooking() throws Exception {
        when(service.approvedBooking(userId, bookingId, true)).thenReturn(bookingDtoResponse);

        mvc.perform(patch(url + "/{bookingId}", bookingId)
                        .header(X_SHARER_USER_ID, userId)
                        .param("approved", "true")
                        .content(mapper.writeValueAsString(bookingDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDtoResponse)));

        verify(service, times(1)).approvedBooking(userId, bookingId, true);
    }

    @Test
    void approvedBooking_whenBookingRejected_thenUpdateBooking() throws Exception {
        when(service.approvedBooking(userId, bookingId, false)).thenReturn(bookingDtoResponse);

        mvc.perform(patch(url + "/{bookingId}", bookingId)
                        .header(X_SHARER_USER_ID, userId)
                        .param("approved", "false")
                        .content(mapper.writeValueAsString(bookingDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDtoResponse)));

        verify(service, times(1)).approvedBooking(userId, bookingId, false);
    }

    @Test
    void getBookingById_whenBookingFound_thenReturnBooking() throws Exception {
        when(service.getBookingById(userId, bookingId)).thenReturn(bookingDtoResponse);

        mvc.perform(get(url + "/" + bookingId)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(bookingDtoResponse))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(bookingDtoResponse)));

        verify(service, times(1)).getBookingById(userId, bookingId);
    }

    @Test
    void getBookingsByBooker_whenBookingFound_thenReturnBooking() throws Exception {
        when(service.getBookingsByBooker(userId, State.ALL)).thenReturn(List.of(bookingDtoResponse));

        mvc.perform(get(url)
                        .header(X_SHARER_USER_ID, userId)
                        .param("state", String.valueOf(State.ALL))
                        .content(mapper.writeValueAsString(List.of(bookingDtoResponse)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDtoResponse))));

        verify(service, times(1)).getBookingsByBooker(userId, State.ALL);
    }

    @Test
    void getBookingsByOwner_whenBookingFound_thenReturnBooking() throws Exception {
        when(service.getBookingsByOwner(userId, State.ALL)).thenReturn(List.of(bookingDtoResponse));

        mvc.perform(get(url + "/owner")
                        .header(X_SHARER_USER_ID, userId)
                        .param("state", String.valueOf(State.ALL))
                        .content(mapper.writeValueAsString(List.of(bookingDtoResponse)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(bookingDtoResponse))));

        verify(service, times(1)).getBookingsByOwner(userId, State.ALL);
    }
}
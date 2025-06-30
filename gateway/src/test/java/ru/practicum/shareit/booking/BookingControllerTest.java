package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @MockBean
    private BookingClient client;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private BookItemRequestDto requestDto;
    private final long userId = 1L;
    private final long bookingId = 1L;

    @Test
    void createBooking_whenBookingValid_thenResponseIsOk() throws Exception {
        requestDto = new BookItemRequestDto(
                bookingId,
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusMinutes(15)
        );

        when(client.createBooking(userId, requestDto)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_whenBookingNotValid_thenResponseIsOk() throws Exception {
        requestDto = new BookItemRequestDto(
                bookingId,
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now()
        );

        when(client.createBooking(userId, requestDto)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approvedBooking_whenApprovedBooking_thenResponseIsOk() throws Exception {
        requestDto = new BookItemRequestDto(
                bookingId,
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusMinutes(15)
        );

        when(client.approvedBooking(userId, bookingId, true)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(patch("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void approvedBooking_whenApprovedBookingNotValid_thenResponseIsOk() throws Exception {
        requestDto = new BookItemRequestDto(
                bookingId,
                LocalDateTime.now().plusMinutes(5),
                LocalDateTime.now().plusMinutes(15)
        );

        when(client.approvedBooking(userId, bookingId, true))
                .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(patch("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_whenBookingFound_thenResponseIsOk() throws Exception {
        when(client.getBookingById(userId, bookingId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBookings_whenBookingsFound_thenResponseIsOk() throws Exception {
        String stateParam = "all";
        Integer from = 0;
        Integer size = 10;

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

        when(client.getBookings(userId, state, from, size)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", stateParam)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwner_whenBookingsFound_thenResponseIsOk() throws Exception {
        String stateParam = "all";
        Integer from = 0;
        Integer size = 10;

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));

        when(client.getBookings(userId, state, from, size)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", stateParam)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingState_whenStateIsEmpty_thrownException() {
        String stateParam = "";

        try {
            BookingState.from(stateParam);
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown state: " + stateParam, e.getMessage());
        }
    }
}
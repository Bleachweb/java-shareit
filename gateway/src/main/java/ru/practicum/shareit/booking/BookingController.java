package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                @Valid @RequestBody BookItemRequestDto requestDto) {
        log.info("Бронирование вещи: {}, пользователем с id: {}", requestDto, userId);
        return bookingClient.createBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approvedBooking(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                  @PathVariable long bookingId,
                                                  @RequestParam boolean approved) {
        log.info("Подтверждение бронирования вещи с id: {} пользователем с id: {}", bookingId, userId);
        return bookingClient.approvedBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                 @PathVariable Long bookingId) {
        log.info("Получение данных о бронировании с id: {}, пользователем с id: {}", bookingId, userId);
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookings(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                              @PositiveOrZero @RequestParam(name = "from",
                                                      defaultValue = "0") Integer from,
                                              @Positive @RequestParam(name = "size",
                                                      defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус: " + stateParam));
        log.info("Получение бронирований {} пользователем с id {} с {} в колличестве {}", stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(X_SHARER_USER_ID) long userId,
                                                     @RequestParam(name = "state",
                                                             defaultValue = "all") String stateParam,
                                                     @PositiveOrZero @RequestParam(name = "from",
                                                             defaultValue = "0") Integer from,
                                                     @Positive @RequestParam(name = "size",
                                                             defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус: " + stateParam));
        log.info("Получение бронирований {} владельцем с id{} с {} в колличестве {}", stateParam, userId, from, size);
        return bookingClient.getBookingsByOwner(userId, state, from, size);
    }
}
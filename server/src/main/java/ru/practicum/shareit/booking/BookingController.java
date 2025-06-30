package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoResponse createBooking(@RequestHeader(X_SHARER_USER_ID) int userId,
                                            @RequestBody BookingDtoRequest bookingDtoRequest) {
        BookingDtoResponse bookingResponse = bookingService.createBooking(userId, bookingDtoRequest);
        log.info("{} - забронирован(а)", bookingResponse);
        return bookingResponse;
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approvedBooking(@RequestHeader(X_SHARER_USER_ID) int userId,
                                              @PathVariable int bookingId,
                                              @RequestParam boolean approved) {

        BookingDtoResponse bookingResponse = bookingService.approvedBooking(userId, bookingId, approved);

        log.info("Бронирование: {} - {}", bookingResponse, approved ? "подтверждено" : "отклонено");
        return bookingResponse;
    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse getBookingById(@RequestHeader(X_SHARER_USER_ID) int userId,
                                             @PathVariable int bookingId) {
        BookingDtoResponse bookingResponse = bookingService.getBookingById(userId, bookingId);
        log.info("Получены данные о бронировании: {}", bookingResponse);
        return bookingResponse;
    }

    @GetMapping
    public List<BookingDtoResponse> getBookingsByBooker(@RequestHeader(X_SHARER_USER_ID) int userId,
                                                        @RequestParam(defaultValue = "ALL") State state) {
        List<BookingDtoResponse> bookingResponses = bookingService.getBookingsByBooker(userId, state);
        log.info("Получен список всех бронирований пользователя c id: {}, со статусом - {} : {}",
                userId, state, bookingResponses);
        return bookingResponses;
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> getBookingsByOwner(@RequestHeader(X_SHARER_USER_ID) int userId,
                                                       @RequestParam(defaultValue = "ALL") State state) {
        List<BookingDtoResponse> bookingResponses = bookingService.getBookingsByOwner(userId, state);
        log.info("Получен список бронирований владельца c id: {}, со статусом - {}: {}", userId, state, bookingResponses);
        return bookingResponses;
    }
}
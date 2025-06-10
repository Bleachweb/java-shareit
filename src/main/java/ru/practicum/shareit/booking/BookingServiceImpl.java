package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnknownStateException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDtoResponse createBooking(int userId, BookingDtoRequest bookingDtoRequest) {
        final int itemId = bookingDtoRequest.getItemId();

        User booker = getUser(userId);
        Item item = getItem(itemId);

        if (booker.getId().equals(item.getUser().getId())) {
            log.warn("Пользователь с id {} является владельцем вещи с id {}", userId, itemId);
            throw new ValidationException("Владелец не может забронировать свою вещь");
        }

        if (!item.isAvailable()) {
            log.warn("Вещи с id {} не доступен(на) для бронирования", itemId);
            throw new ValidationException(item + ", не доступен(на) для бронирования");
        }

        if (bookingDtoRequest.getStart().equals(bookingDtoRequest.getEnd())) {
            log.warn("Дата начала бронирования равна дате окончания");
            throw new ValidationException("Дата начала бронирования должна отличаться от даты окончания");
        }

        if (bookingDtoRequest.getStart().isAfter(bookingDtoRequest.getEnd())) {
            log.warn("Дата окончания бронирования раньше даты начала бронирования");
            throw new ValidationException("Дата окончания бронирования раньше даты начала бронирования");
        }

        Booking booking = bookingMapper.toBooking(bookingDtoRequest, booker, item);
        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoResponse approvedBooking(int userId, int bookingId, Boolean approved) {

        Booking booking = getBooking(bookingId);
        final int itemId = booking.getItem().getId();
        Item item = getItem(itemId);
        final int ownerId = item.getUser().getId();

        if (ownerId != userId) {
            log.warn("У {} не найдена вещь {}", item.getUser().getId(), item.getId());
            throw new ValidationException(" У '" + item.getUser().getName() +
                    "' не найден(а): '" + item.getName());
        }

        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            log.warn("Вещь уже имеет статус: {}", booking.getStatus());
            throw new ValidationException("Вещь уже имеет статус: '" + booking.getStatus());
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking approvedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingDto(approvedBooking);
    }

    @Override
    public BookingDtoResponse getBookingById(int userId, int bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id - " + bookingId + " не найдено"));

        User owner = booking.getItem().getUser();
        User booker = booking.getBooker();

        int ownerId = owner.getId();
        int bookerId = booker.getId();

        if (ownerId != userId && bookerId != userId) {
            log.warn("У {} и {} не найдена вещь {}",owner.getId(), booker.getId(), booking.getItem().getId());
            throw new NotFoundException("У '" + owner.getName() + "' и '" + booker.getName() +
                    "' не найден предмет: '" + booking.getItem().getName());
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDtoResponse> getBookingsByBooker(int bookerId, State state) {

        getUser(bookerId);
        List<Booking> bookingDtoResponses;

        switch (state) {
            case ALL -> {
                log.info("Запрос на получение всех бронирований пользователя с id: {}", bookerId);
                bookingDtoResponses = bookingRepository.findAllBookingsByBookerIdOrderByStartDesc(bookerId);
            }
            case CURRENT -> {
                log.info("Запрос на получение текущих бронирований пользователя с id: {}", bookerId);
                bookingDtoResponses = bookingRepository.findAllCurrentBookingsByBookerId(bookerId, LocalDateTime.now());
            }
            case PAST -> {
                log.info("Запрос на получение завершенных бронирований пользователя с id: {}", bookerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now());
            }
            case FUTURE -> {
                log.info("Запрос на получение будущих бронирований пользователя с id: {}", bookerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now());
            }
            case WAITING -> {
                log.info("Запрос на получение бронирований пользователя с id: {}, ожидающих подтверждения", bookerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            }
            case REJECTED -> {
                log.info("Запрос на получение отклоненных бронирований пользователя с id: {}", bookerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
            }
            default -> {
                log.error("Некорректный статус бронирования пользователя: {}", state);
                throw new UnknownStateException("Некорректный статус бронирования: " + state);
            }
        }
        return bookingDtoResponses.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoResponse> getBookingsByOwner(int ownerId, State state) {

        User user = getUser(ownerId);
        Set<Integer> itemIds = itemRepository.findAllItemsByUserOrderByIdAsc(user).stream()
                .map(Item::getId)
                .collect(Collectors.toSet());
        List<Booking> bookingDtoResponses;

        switch (state) {
            case ALL -> {
                log.info("Запрос на получение всех бронирований владельцем с id: {}", ownerId);
                bookingDtoResponses = bookingRepository.findAllBookingsByItemIdInOrderByStartDesc(itemIds);
            }
            case CURRENT -> {
                log.info("Запрос на получение текущих бронирований владельцем с id: {}", ownerId);
                bookingDtoResponses = bookingRepository.findAllCurrentBookingsByOwnerId(ownerId, LocalDateTime.now());
            }
            case PAST -> {
                log.info("Запрос на получение завершенных бронирований владельцем с id: {}", ownerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByItemUserIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now());
            }
            case FUTURE -> {
                log.info("Запрос на получение будущих бронирований владельцем с id: {}", ownerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByItemUserIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now());
            }
            case WAITING -> {
                log.info("Запрос на получение бронирований владельцем с id: {}, ожидающих подтверждение", ownerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByItemUserIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);

            }
            case REJECTED -> {
                log.info("Запрос на получение отклоненных бронирований владельцем с id: {}", ownerId);
                bookingDtoResponses = bookingRepository
                        .findAllBookingsByItemUserIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
            }
            default -> {
                log.error("Некорректный статус бронирования владельца: {}", state);
                throw new UnknownStateException("Некорректный статус бронирования: " + state);
            }
        }
        return bookingDtoResponses.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private User getUser(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + userId + " не найден"));
    }

    private Item getItem(int itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id - " + itemId + " не найдена"));
    }

    private Booking getBooking(int bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id - " + bookingId + " не найдено"));
    }
}
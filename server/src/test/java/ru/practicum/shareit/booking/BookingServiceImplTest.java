package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    private final EntityManager em;
    private final BookingService service;
    private final UserService userService;
    private final ItemService itemService;

    private User owner;
    private User booker;
    private UserDto bookerDto;
    private Item item;
    private ItemDtoRequest itemDtoRequest;
    private ItemDtoResponse itemDtoResponse;
    private Booking booking;
    private BookingDtoRequest bookingDtoRequest;
    private BookingDtoResponse bookingDtoResponse;

    private int ownerId;
    private int bookerId;
    private int itemId;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        UserDto ownerDto = new UserDto();
        ownerDto.setName("Owner Name");
        ownerDto.setEmail("owner@email.com");
        ownerDto = userService.createUser(ownerDto);
        ownerId = ownerDto.getId();

        bookerDto = new UserDto();
        bookerDto.setName("Booker Name");
        bookerDto.setEmail("booker@email.com");
        bookerDto = userService.createUser(bookerDto);
        bookerId = bookerDto.getId();

        owner = new User();
        owner.setId(ownerId);
        owner.setName("Owner Name");
        owner.setEmail("owner@email.com");

        booker = new User();
        booker.setId(bookerId);
        booker.setName("Booker Name");
        booker.setEmail("booker@email.com");

        start = LocalDateTime.now();
        end = start.plusNanos(5);

        itemDtoRequest = ItemDtoRequest.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        itemDtoResponse = itemService.createItem(ownerId, itemDtoRequest);
        itemId = itemDtoResponse.getId();

        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();
    }

    @Test
    void createBooking_whenBookingValid_thenSavedBooking() {
        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.id = :bookingId", Booking.class);
        booking = query.setParameter("bookingId", bookingDtoResponse.getId()).getSingleResult();

        assertThat(booking, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDtoResponse.getStart())),
                hasProperty("end", equalTo(bookingDtoResponse.getEnd())),
                hasProperty("status", equalTo(bookingDtoResponse.getStatus()))
        ));
    }

    @Test
    void createBooking_whenBookerDoesntExist_thenNotFoundExceptionThrown() {
        final int userId = 0;
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.createBooking(userId, bookingDtoRequest));
        assertEquals("Пользователь с id - " + userId + " не найден", notFoundException.getMessage());
    }

    @Test
    void createBooking_whenItemDoesntExist_thenNotFoundExceptionThrown() {
        itemId = 0;
        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.createBooking(ownerId, bookingDtoRequest));
        assertEquals("Вещь с id - " + itemId + " не найдена", notFoundException.getMessage());
    }

    @Test
    void createBooking_whenBookingItemByOwner_thenValidationExceptionThrown() {
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.createBooking(ownerId, bookingDtoRequest));
        assertEquals("Владелец не может забронировать свою вещь",
                validationException.getMessage());
    }

    @Test
    void createBooking_whenItemNotAvailableForBooking_thenValidationExceptionThrown() {
        itemDtoRequest = ItemDtoRequest.builder()
                .name("Item")
                .description("Description")
                .available(false)
                .build();

        itemDtoResponse = itemService.createItem(ownerId, itemDtoRequest);
        itemId = itemDtoResponse.getId();
        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        item = query.setParameter("itemId", itemDtoResponse.getId()).getSingleResult();

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.createBooking(bookerId, bookingDtoRequest));
        assertEquals(item + ", не доступен(на) для бронирования",
                validationException.getMessage());
    }

    @Test
    void createBooking_whenStartEqualEnd_thenValidationExceptionThrown() {
        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(start)
                .end(start)
                .build();

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.createBooking(bookerId, bookingDtoRequest));
        assertEquals("Дата начала бронирования должна отличаться от даты окончания",
                validationException.getMessage());
    }

    @Test
    void createBooking_whenEndIsBeforeStart_thenValidationExceptionThrown() {
        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(end)
                .end(start)
                .build();

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.createBooking(bookerId, bookingDtoRequest));
        assertEquals("Дата окончания бронирования раньше даты начала бронирования",
                validationException.getMessage());
    }

    @Test
    void approvedBooking_whenBookingApproved_thenUpdateBooking() {
        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);
        bookingDtoResponse = service.approvedBooking(ownerId, bookingDtoResponse.getId(), true);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.id = :bookingId", Booking.class);
        booking = query.setParameter("bookingId", bookingDtoResponse.getId()).getSingleResult();

        assertThat(booking, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDtoResponse.getStart())),
                hasProperty("end", equalTo(bookingDtoResponse.getEnd())),
                hasProperty("status", equalTo(bookingDtoResponse.getStatus()))
        ));
    }

    @Test
    void approvedBooking_whenOwnerDoesNotHaveItem_thenValidationExceptionThrown() {
        itemDtoRequest = ItemDtoRequest.builder()
                .name("Item 2")
                .description("Description 2")
                .available(true)
                .build();

        itemDtoResponse = itemService.createItem(bookerId, itemDtoRequest);
        itemId = itemDtoResponse.getId();
        bookingDtoRequest = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        item = query.setParameter("itemId", itemId).getSingleResult();
        bookingDtoResponse = service.createBooking(ownerId, bookingDtoRequest);

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.approvedBooking(ownerId, bookingDtoResponse.getId(), true));
        assertEquals("У " + item.getUser().getName() + " - не найден предмет: " + item.getName() + "!",
                validationException.getMessage());
    }

    @Test
    void approvedBooking_whenBookingBeenApproved_thenValidationExceptionThrown() {
        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);
        bookingDtoResponse = service.approvedBooking(ownerId, bookingDtoResponse.getId(), true);

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.approvedBooking(ownerId, bookingDtoResponse.getId(), true));
        assertEquals("Вещь уже имеет статус: " + bookingDtoResponse.getStatus(),
                validationException.getMessage());
    }

    @Test
    void approvedBooking_whenBookingRejected_thenUpdateBooking() {
        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);
        bookingDtoResponse = service.approvedBooking(ownerId, bookingDtoResponse.getId(), false);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.id = :bookingId", Booking.class);
        booking = query.setParameter("bookingId", bookingDtoResponse.getId()).getSingleResult();

        assertThat(booking, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDtoResponse.getStart())),
                hasProperty("end", equalTo(bookingDtoResponse.getEnd())),
                hasProperty("status", equalTo(bookingDtoResponse.getStatus()))
        ));
    }

    @Test
    void getBookingById_whenBookingDoesntExist_thenValidationExceptionThrown() {
        final int bookingId = 0;

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.getBookingById(ownerId, bookingId));
        assertEquals("Бронирование с id - " + bookingId + " не найдено", notFoundException.getMessage());
    }

    @Test
    void getBookingById_whenBookingFound_thenReturnBooking() {
        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);
        final int bookingId = bookingDtoResponse.getId();
        BookingDtoResponse response = service.getBookingById(ownerId, bookingId);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.id = :bookingId", Booking.class);
        booking = query.setParameter("bookingId", bookingDtoResponse.getId()).getSingleResult();

        assertThat(booking, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(response.getStart())),
                hasProperty("end", equalTo(response.getEnd())),
                hasProperty("status", equalTo(response.getStatus())),
                hasProperty("item", equalTo(response.getItem())),
                hasProperty("booker", equalTo(response.getBooker()))
        ));
    }

    @Test
    void getBookingById_whenItemNotFound_thenValidationExceptionThrown() {
        UserDto otherUser = new UserDto();
        otherUser.setName("Other User");
        otherUser.setEmail("other@email.com");
        otherUser = userService.createUser(otherUser);
        final int otherUserId = otherUser.getId();

        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);
        final int bookingId = bookingDtoResponse.getId();

        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.getBookingById(otherUserId, bookingId));
        assertEquals("У " + otherUser.getName() + " и " + bookerDto.getName() +
                        " не найден предмет: " + bookingDtoResponse.getItem().getName(),
                notFoundException.getMessage());
    }

    @Test
    void getBookingsByBooker_whenRequestAllBooking_thenResponseIsOkWithBookingsListInBody() {
        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);
        final int bookingId = bookingDtoResponse.getId();
        List<BookingDtoResponse> responses = service.getBookingsByBooker(bookerId, State.ALL);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.booker = :booker", Booking.class);
        List<Booking> bookings = query.setParameter("booker", booker).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookingId, bookings.getFirst().getId());
        assertEquals(bookings.getFirst().getBooker(), responses.getFirst().getBooker());
    }

    @Test
    void getBookingsByBooker_whenRequestCurrentBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByBooker(bookerId, State.CURRENT);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.booker = :booker" +
                        " AND b.start <= LOCAL DATETIME AND b.end >= LOCAL DATETIME", Booking.class);
        List<Booking> bookings = query.setParameter("booker", booker).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getBooker(), responses.getFirst().getBooker());
    }

    @Test
    void getBookingsByBooker_whenRequestPastBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByBooker(bookerId, State.PAST);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.booker = :booker" +
                        " AND b.end <= LOCAL DATETIME", Booking.class);
        List<Booking> bookings = query.setParameter("booker", booker).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getBooker(), responses.getFirst().getBooker());
    }

    @Test
    void getBookingsByBooker_whenRequestFutureBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByBooker(bookerId, State.FUTURE);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.booker = :booker" +
                        " AND b.start > LOCAL DATETIME", Booking.class);
        List<Booking> bookings = query.setParameter("booker", booker).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getBooker(), responses.getFirst().getBooker());
    }

    @Test
    void getBookingsByBooker_whenRequestWaitingBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByBooker(bookerId, State.WAITING);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.booker = :booker" +
                        " AND b.status = :status", Booking.class);
        List<Booking> bookings = query.setParameter("booker", booker)
                .setParameter("status", BookingStatus.WAITING).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getBooker(), responses.getFirst().getBooker());
    }

    @Test
    void getBookingsByBooker_whenRequestRejectedBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByBooker(bookerId, State.REJECTED);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.booker = :booker" +
                        " AND b.status = :status", Booking.class);
        List<Booking> bookings = query.setParameter("booker", booker)
                .setParameter("status", BookingStatus.REJECTED).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getBooker(), responses.getFirst().getBooker());
    }

    @Test
    void getBookingsByOwner_whenRequestAllBooking_thenResponseIsOkWithBookingsListInBody() {
        bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);
        final int bookingId = bookingDtoResponse.getId();
        List<BookingDtoResponse> responses = service.getBookingsByOwner(ownerId, State.ALL);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.item.user = :owner", Booking.class);
        List<Booking> bookings = query.setParameter("owner", owner).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookingId, bookings.getFirst().getId());
        assertEquals(bookings.getFirst().getItem().getUser(), responses.getFirst().getItem().getUser());
    }

    @Test
    void getBookingsByOwner_whenRequestCurrentBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByOwner(ownerId, State.CURRENT);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.item.user = :owner " +
                        "AND b.start <= LOCAL DATETIME AND b.end >= LOCAL DATETIME", Booking.class);
        List<Booking> bookings = query.setParameter("owner", owner).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getItem().getUser(), responses.getFirst().getItem().getUser());
    }

    @Test
    void getBookingsByOwner_whenRequestPastBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByOwner(ownerId, State.PAST);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.item.user = :owner " +
                        "AND b.end <= LOCAL DATETIME", Booking.class);
        List<Booking> bookings = query.setParameter("owner", owner).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getItem().getUser(), responses.getFirst().getItem().getUser());
    }

    @Test
    void getBookingsByOwner_whenRequestFutureBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByOwner(ownerId, State.FUTURE);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.item.user = :owner " +
                        "AND b.start > LOCAL DATETIME", Booking.class);
        List<Booking> bookings = query.setParameter("owner", owner).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getItem().getUser(), responses.getFirst().getItem().getUser());
    }

    @Test
    void getBookingsByOwner_whenRequestWaitingBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByOwner(ownerId, State.WAITING);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.item.user = :owner " +
                        "AND b.status = :status", Booking.class);
        List<Booking> bookings = query.setParameter("owner", owner)
                .setParameter("status", BookingStatus.WAITING).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getItem().getUser(), responses.getFirst().getItem().getUser());
    }

    @Test
    void getBookingsByOwner_whenRequestRejectedBooking_thenResponseIsOkWithBookingsListInBody() {
        setBooking();
        List<BookingDtoResponse> responses = service.getBookingsByOwner(ownerId, State.REJECTED);

        TypedQuery<Booking> query = em
                .createQuery("SELECT b FROM Booking AS b WHERE b.item.user = :owner " +
                        "AND b.status = :status", Booking.class);
        List<Booking> bookings = query.setParameter("owner", owner)
                .setParameter("status", BookingStatus.REJECTED).getResultList();

        assertThat(bookings, hasSize(responses.size()));
        assertEquals(bookings.getFirst().getItem().getUser(), responses.getFirst().getItem().getUser());
    }

    private void setBooking() {
        for (int i = 1; i <= 5; i++) {
            itemDtoRequest = ItemDtoRequest.builder()
                    .name("Item " + i)
                    .description("Description " + i)
                    .available(true)
                    .build();

            itemDtoResponse = itemService.createItem(ownerId, itemDtoRequest);
            itemId = itemDtoResponse.getId();

            bookingDtoRequest = BookingDtoRequest.builder()
                    .itemId(itemId)
                    .start(start)
                    .end(end)
                    .build();

            if (i % 2 == 0) {
                bookingDtoRequest.setEnd(end.plusMinutes(i));
            }

            if (i == 5) {
                bookingDtoRequest.setStart(start.plusMinutes(1));
                bookingDtoRequest.setEnd(end.plusMinutes(i));
            }

            bookingDtoResponse = service.createBooking(bookerId, bookingDtoRequest);

            if (i == 1) {
                service.approvedBooking(ownerId, bookingDtoResponse.getId(), false);
            }
        }
    }
}
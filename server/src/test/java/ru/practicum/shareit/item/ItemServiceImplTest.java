package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentDtoRequest;
import ru.practicum.shareit.comment.CommentDtoResponse;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestDtoRequest;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    private final EntityManager em;
    private final ItemService service;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemRequestService itemRequestService;

    private UserDto userDto;
    private Item item;
    private ItemDtoRequest itemDtoRequest;
    private ItemDtoResponse itemDtoResponse;
    private CommentDtoRequest commentDtoRequest;
    private CommentDtoResponse commentDtoResponse;
    private Comment comment;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("email@email.com");
        userDto = userService.createUser(userDto);

        itemDtoRequest = new ItemDtoRequest();
        itemDtoRequest.setName("Name");
        itemDtoRequest.setDescription("Description");
        itemDtoRequest.setAvailable(true);

        item = new Item();
        itemDtoResponse = new ItemDtoResponse();
    }

    @Test
    void createItem_whenItemValid_thenSavedItem() {
        itemDtoRequest.setRequestId(0);
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        item = query.setParameter("itemId", itemDtoResponse.getId()).getSingleResult();

        assertThat(item, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDtoResponse.getName())),
                hasProperty("description", equalTo(itemDtoResponse.getDescription())),
                hasProperty("available", equalTo(itemDtoResponse.getAvailable()))
        ));
    }

    @Test
    void createItem_whenItemOnRequestValid_thenSavedItem() {
        ItemRequestDtoRequest itemRequestDtoRequest = new ItemRequestDtoRequest();
        itemRequestDtoRequest.setDescription("Description");
        ItemRequestDto request = itemRequestService.createRequest(userDto.getId(), itemRequestDtoRequest);
        itemDtoRequest.setRequestId(request.getId());
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        item = query.setParameter("itemId", itemDtoResponse.getId()).getSingleResult();

        assertThat(item, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDtoResponse.getName())),
                hasProperty("description", equalTo(itemDtoResponse.getDescription())),
                hasProperty("available", equalTo(itemDtoResponse.getAvailable()))
        ));
    }

    @Test
    void createItem_whenItemOnRequestNotFound_thenNotFoundExceptionThrown() {
        final int requestId = 1;
        itemDtoRequest.setRequestId(requestId);
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.createItem(userDto.getId(), itemDtoRequest));
        assertEquals("Запрос с id - " + requestId + " не найден!", notFoundException.getMessage());
    }

    @Test
    void createItem_whenUserNotFound_thenNotFoundExceptionThrown() {
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.createItem(0, itemDtoRequest));
        assertEquals("Пользователь с id - 0 не найден",
                notFoundException.getMessage());
    }

    @Test
    void updateItem_whenUpdateItemDescription_thenUpdateItem() {
        ItemDtoRequest updateItemDtoRequest = new ItemDtoRequest();
        updateItemDtoRequest.setName("");
        updateItemDtoRequest.setDescription("Updated Description");
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        ItemDtoResponse updatedItem = service.updateItem(userDto.getId(),
                itemDtoResponse.getId(), updateItemDtoRequest);

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        Item itemUpdated = query.setParameter("itemId", updatedItem.getId()).getSingleResult();

        assertThat(itemUpdated, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDtoRequest.getName())),
                hasProperty("description", equalTo(updateItemDtoRequest.getDescription())),
                hasProperty("available", equalTo(itemDtoRequest.getAvailable()))
        ));
    }

    @Test
    void updateItem_whenUpdateItemName_thenUpdateItem() {
        ItemDtoRequest updateItemDtoRequest = new ItemDtoRequest();
        updateItemDtoRequest.setName("Updated Name");
        updateItemDtoRequest.setDescription("");
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        ItemDtoResponse updatedItem = service.updateItem(userDto.getId(),
                itemDtoResponse.getId(), updateItemDtoRequest);

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        Item itemUpdated = query.setParameter("itemId", updatedItem.getId()).getSingleResult();

        assertThat(itemUpdated, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(updateItemDtoRequest.getName())),
                hasProperty("description", equalTo(itemDtoRequest.getDescription())),
                hasProperty("available", equalTo(itemDtoRequest.getAvailable()))
        ));
    }

    @Test
    void updateItem_whenUpdateItemAvailable_thenUpdateItem() {
        ItemDtoRequest updateItemDtoRequest = new ItemDtoRequest();
        updateItemDtoRequest.setAvailable(false);
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        ItemDtoResponse updatedItem = service.updateItem(userDto.getId(),
                itemDtoResponse.getId(), updateItemDtoRequest);

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        Item itemUpdated = query.setParameter("itemId", updatedItem.getId()).getSingleResult();

        assertThat(itemUpdated, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDtoRequest.getName())),
                hasProperty("description", equalTo(itemDtoRequest.getDescription())),
                hasProperty("available", equalTo(updateItemDtoRequest.getAvailable()))
        ));
    }

    @Test
    void getItemById_whenItemFound_thenReturnItem() {
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        ItemDto itemDto = service.getItemById(userDto.getId(), itemDtoResponse.getId());

        TypedQuery<Item> query = em
                .createQuery("SELECT i FROM Item AS i WHERE i.id = :itemId", Item.class);
        item = query.setParameter("itemId", itemDtoResponse.getId()).getSingleResult();

        assertThat(item, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDto.getName())),
                hasProperty("description", equalTo(itemDto.getDescription())),
                hasProperty("available", equalTo(itemDto.getAvailable()))
        ));
    }

    @Test
    void getItemById_whenItemNotFound_thenNotFoundExceptionThrown() {
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.getItemById(userDto.getId(), 0));
        assertEquals("Предмет с id - 0 не найден", notFoundException.getMessage());
    }

    @Test
    void getItems_whenInvoked_thenResponseIsOkWithItemsListInBody() {
        List<ItemDtoResponse> requestsCreatedDto = IntStream.range(0, 5)
                .mapToObj(i -> service.createItem(userDto.getId(), itemDtoRequest))
                .toList();

        List<ItemDto> items = service.getItems(userDto.getId());

        items.forEach(request -> assertThat(items, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDtoRequest.getName())),
                hasProperty("description", equalTo(itemDtoRequest.getDescription())),
                hasProperty("available", equalTo(itemDtoRequest.getAvailable()))
        ))));
        assertEquals(requestsCreatedDto.size(), items.size());
        assertEquals(requestsCreatedDto.getFirst().getId(), items.getFirst().getId());
    }

    @Test
    void getItems_whenInvoked_thenResponseIsOkWithItemsBookingsAndCommentsListInBody() throws InterruptedException {
        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker@booker.com");
        UserDto booker = userService.createUser(bookerDto);

        final int bookerId = booker.getId();
        final int userId = userDto.getId();

        itemDtoResponse = service.createItem(userId, itemDtoRequest);
        final int itemId = itemDtoResponse.getId();

        BookingDtoRequest booking = new BookingDtoRequest();

        booking.setItemId(itemId);
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusNanos(1));
        BookingDtoResponse last = bookingService.createBooking(bookerId, booking);

        Thread.sleep(10);

        commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");
        commentDtoResponse = service.addComment(bookerId, itemId, commentDtoRequest);

        booking.setItemId(itemId);
        booking.setStart(LocalDateTime.now().plusMinutes(1));
        booking.setEnd(LocalDateTime.now().plusMinutes(5));
        BookingDtoResponse next = bookingService.createBooking(bookerId, booking);

        ItemDto itemDto = service.getItemById(userId, itemId);

        TypedQuery<Comment> queryGetComment = em
                .createQuery("SELECT c FROM Comment AS c WHERE c.id = :commentId", Comment.class);
        comment = queryGetComment.setParameter("commentId", commentDtoResponse.getId()).getSingleResult();

        assertThat(itemDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDtoResponse.getName())),
                hasProperty("description", equalTo(itemDtoResponse.getDescription())),
                hasProperty("available", equalTo(itemDtoResponse.getAvailable())),
                hasProperty("comments", hasItem(allOf(
                        hasProperty("authorName", is(comment.getAuthor().getName())),
                        hasProperty("text", is(comment.getText()))
                ))),
                hasProperty("nextBooking", (allOf(
                        hasProperty("status", is(next.getStatus())),
                        hasProperty("bookerId", is(next.getBooker().getId()))
                ))),
                hasProperty("lastBooking", (allOf(
                        hasProperty("id", is(last.getId())),
                        hasProperty("end", is(last.getEnd()))
                ))),
                hasProperty("owner", (allOf(
                                hasProperty("name", is(userDto.getName())),
                                hasProperty("email", is(userDto.getEmail()))
                        ))
                )));
    }

    @Test
    void searchItems_whenInvoked_thenResponseIsOkWithUsersListInBody() {
        final String text = "Name";
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);
        List<ItemDtoResponse> items = service.searchItems(userDto.getId(), text);

        items.forEach(itemDtoResponse -> assertEquals(text, itemDtoResponse.getName()));
    }

    @Test
    void searchItems_whenTextIsEmpty_thenResponseIsOkWithEmptyListInBody() {
        final String text = "";
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);
        List<ItemDtoResponse> items = service.searchItems(userDto.getId(), text);

        items.forEach(itemDtoResponse -> assertEquals(text, itemDtoResponse.getName()));
        assertEquals(0, items.size());
    }

    @Test
    void addComment_whenCommentValid_thenReturnComment() throws InterruptedException {
        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker@booker.com");

        UserDto booker = userService.createUser(bookerDto);

        commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        BookingDtoRequest booking = new BookingDtoRequest();
        booking.setItemId(itemDtoResponse.getId());
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusNanos(1));
        bookingService.createBooking(booker.getId(), booking);

        Thread.sleep(1);

        commentDtoResponse = service.addComment(booker.getId(), itemDtoResponse.getId(), commentDtoRequest);

        TypedQuery<Comment> queryGetComment = em
                .createQuery("SELECT c FROM Comment AS c WHERE c.id = :commentId", Comment.class);
        comment = queryGetComment.setParameter("commentId", commentDtoResponse.getId()).getSingleResult();

        ItemDto itemDto = service.getItemById(userDto.getId(), itemDtoResponse.getId());

        assertEquals(1, itemDto.getComments().size());
        assertThat(comment, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("text", equalTo(commentDtoResponse.getText())),
                hasProperty("item", (allOf(
                        hasProperty("name", is(itemDto.getName())),
                        hasProperty("description", is(itemDto.getDescription())),
                        hasProperty("user", (allOf(
                                hasProperty("name", is(userDto.getName())),
                                hasProperty("email", is(userDto.getEmail())))))))),
                hasProperty("author", (allOf(
                        hasProperty("name", is(booker.getName())),
                        hasProperty("email", is(booker.getEmail()))
                )))
        ));
    }

    @Test
    void addComment_whenBookingNotFound_thenValidationExceptionThrown() {
        commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.addComment(userDto.getId(), itemDtoResponse.getId(), commentDtoRequest));
        assertEquals("Для вещи с id: " + itemDtoResponse.getId() + " бронирования не было",
                validationException.getMessage());
    }

    @Test
    void addComment_whenBookerNotBooking_thenValidationExceptionThrown() throws InterruptedException {
        UserDto bookerDto = new UserDto();
        bookerDto.setName("Booker");
        bookerDto.setEmail("booker@booker.com");

        UserDto booker = userService.createUser(bookerDto);

        commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");
        itemDtoResponse = service.createItem(userDto.getId(), itemDtoRequest);

        BookingDtoRequest booking = new BookingDtoRequest();
        booking.setItemId(itemDtoResponse.getId());
        booking.setStart(LocalDateTime.now());
        booking.setEnd(LocalDateTime.now().plusNanos(1));
        bookingService.createBooking(booker.getId(), booking);

        Thread.sleep(1);

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.addComment(userDto.getId(), itemDtoResponse.getId(), commentDtoRequest));
        assertEquals("Пользователь с id: " + userDto.getId() + " не бронировал вещь с id: " +
                        itemDtoResponse.getId() + " или срок бронирования не истек",
                validationException.getMessage());
    }

}
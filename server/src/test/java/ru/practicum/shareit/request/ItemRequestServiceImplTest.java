package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

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
class ItemRequestServiceImplTest {

    private final EntityManager em;
    private final ItemRequestService service;
    private final UserService userService;

    private ItemRequestDtoRequest requestCreated;
    private ItemRequestDto itemRequestDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {

        userDto = new UserDto();
        userDto.setName("Name");
        userDto.setEmail("email@email.com");
        userDto = userService.createUser(userDto);

        requestCreated = new ItemRequestDtoRequest();
        requestCreated.setDescription("Description");

        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setId(1);
        itemRequestDto.setDescription("Description");
    }

    @Test
    void createRequest_whenRequestValid_thenSavedRequest() {
        itemRequestDto = service.createRequest(userDto.getId(), requestCreated);

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT r FROM ItemRequest AS r where r.id = :requestId", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("requestId", itemRequestDto.getId()).getSingleResult();

        assertThat(itemRequest, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requestCreated.getDescription()))));
    }

    @Test
    void createRequest_whenRequestNotValid_thenNotFoundExceptionThrown() {
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.createRequest(0, requestCreated));
        assertEquals("Пользователь с id - 0 не найден!",
                notFoundException.getMessage());
    }

    @Test
    void getRequestById_whenRequestFound_thenReturnRequest() {
        itemRequestDto = service.createRequest(userDto.getId(), requestCreated);
        ItemRequestDto request = service.getRequestById(userDto.getId(), itemRequestDto.getId());

        assertThat(request, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requestCreated.getDescription()))));
    }

    @Test
    void getRequestById_whenRequestNotFound_thenNotFoundExceptionThrown() {
        NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> service.getRequestById(userDto.getId(), 0));
        assertEquals("Запрос с id - 0 не найден!", notFoundException.getMessage());
    }

    @Test
    void getAllRequestByUserId_whenInvoked_thenResponseIsOkWithUsersListInBody() {
        List<ItemRequestDto> requestsDto = IntStream.range(0, 5)
                .mapToObj(i -> service.createRequest(userDto.getId(), requestCreated))
                .toList();

        List<ItemRequestDto> requests = service.getAllRequestByUserId(userDto.getId());

        requests.forEach(request -> assertThat(requests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(request.getDescription()))
        ))));
        assertEquals(requestsDto.size(), requests.size());
        assertEquals(requestsDto.getFirst().getId(), requests.getFirst().getId());
    }

    @Test
    void getAllRequest_whenInvoked_thenResponseIsOkWithUsersListInBody() {
        List<ItemRequestDto> requestsDto = IntStream.range(0, 5)
                .mapToObj(i -> service.createRequest(userDto.getId(), requestCreated))
                .toList();

        List<ItemRequestDto> requests = service.getAllRequest();

        requests.forEach(request -> assertThat(requests, hasItem(allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requestCreated.getDescription()))
        ))));
        assertEquals(requestsDto.size(), requests.size());
        assertEquals(requestsDto.getFirst().getId(), requests.getFirst().getId());
    }
}
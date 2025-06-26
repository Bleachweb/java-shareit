package ru.practicum.shareit.item;

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
import ru.practicum.shareit.comment.CommentDtoRequest;
import ru.practicum.shareit.comment.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    @Mock
    private ItemService service;

    @InjectMocks
    private ItemController controller;

    private MockMvc mvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private ItemDto itemDto;
    private User user;
    private ItemDtoResponse createdItemDto;
    private ItemDtoResponse itemDtoResponse;
    private ItemDtoRequest itemDtoRequest;

    private final String url = "/items";
    private final int itemId = 1;
    private final int userId = 1;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        user = new User();
        user.setId(userId);
        user.setName("Test");
        user.setEmail("test@test.com");

        itemDtoRequest = new ItemDtoRequest();
        itemDtoRequest.setName("Test");
        itemDtoRequest.setDescription("Test");
        itemDtoRequest.setAvailable(true);

        createdItemDto = new ItemDtoResponse();
        createdItemDto.setId(itemId);
        createdItemDto.setName("Test");
        createdItemDto.setDescription("Test");
        createdItemDto.setAvailable(true);

        itemDtoResponse = new ItemDtoResponse();
        itemDtoResponse.setId(itemId);
        itemDtoResponse.setName("Test");
        itemDtoResponse.setDescription("Test");
        itemDtoResponse.setAvailable(true);
        itemDtoResponse.setOwner(user);

        itemDto = new ItemDto();
        itemDto.setId(itemId);
        itemDto.setName("Test");
        itemDto.setDescription("Test");
        itemDto.setAvailable(true);
        itemDto.setOwner(user);
    }

    @Test
    void createItem_whenItemValid_thenSavedItem() throws Exception {
        when(service.createItem(userId, itemDtoRequest)).thenReturn(createdItemDto);

        mvc.perform(post(url)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdItemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(createdItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(createdItemDto.getAvailable())));

        verify(service, times(1)).createItem(userId, itemDtoRequest);
    }

    @Test
    void updateItem_whenItemValid_thenUpdateItem() throws Exception {
        when(service.updateItem(userId, itemId, itemDtoRequest)).thenReturn(itemDtoResponse);

        mvc.perform(patch(url + "/" + itemId)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDtoResponse))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoResponse.getAvailable())));

        verify(service, times(1)).updateItem(userId, itemId, itemDtoRequest);
    }

    @Test
    void getItemById_whenItemFound_thenReturnItem() throws Exception {
        when(service.getItemById(userId, itemId)).thenReturn(itemDto);

        mvc.perform(get(url + "/" + itemId)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDtoResponse.getAvailable())));

        verify(service, times(1)).getItemById(userId, itemId);
    }

    @Test
    void getAllItems_whenInvoked_thenResponseIsOkWithItemsListInBody() throws Exception {
        List<ItemDto> items = List.of(itemDto);

        when(service.getItems(userId)).thenReturn(items);

        mvc.perform(get(url)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(items))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service, times(1)).getItems(userId);
    }

    @Test
    void searchItems_whenInvoked_thenResponseIsOkWithRequestsListInBody() throws Exception {
        final String text = "Test";
        List<ItemDtoResponse> items = List.of(itemDtoResponse);

        when(service.searchItems(itemId, text)).thenReturn(items);

        mvc.perform(get(url + "/search?text=" + text)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(items))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(items)));

        verify(service, times(1)).searchItems(itemId, text);
    }

    @Test
    void addComment_whenCommentValid_thenSavedComment() throws Exception {
        CommentDtoRequest commentDtoRequest = new CommentDtoRequest();
        commentDtoRequest.setText("Comment");

        CommentDtoResponse commentDtoResponse = new CommentDtoResponse();
        commentDtoResponse.setId(1);
        commentDtoResponse.setText("Comment");
        commentDtoResponse.setAuthorName(user.getName());

        when(service.addComment(userId, itemId, commentDtoRequest)).thenReturn(commentDtoResponse);

        mvc.perform(post(url + "/{itemId}/comment", itemId)
                        .header(X_SHARER_USER_ID, userId)
                        .content(mapper.writeValueAsString(commentDtoRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDtoResponse.getId()), Integer.class))
                .andExpect(jsonPath("$.text", is(commentDtoResponse.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDtoResponse.getAuthorName())));

        verify(service, times(1)).addComment(userId, itemId, commentDtoRequest);
    }
}
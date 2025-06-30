package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    private ItemRequestDto requestDto;
    private int userId;
    private int itemId;

    @BeforeEach
    void setUp() {
        itemId = 1;
        userId = 1;

        requestDto = new ItemRequestDto(itemId, "item", "description", true, null);
    }

    @Test
    void createItem_whenItemValid_thenResponseIsOk() throws Exception {
        when(itemClient.createItem(userId, requestDto)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_whenItemNameNotValid_thenReturnBadRequest() throws Exception {
        requestDto = new ItemRequestDto(itemId, "", "description", true, null);

        when(itemClient.createItem(userId, requestDto)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_whenItemDescriptionNotValid_thenReturnBadRequest() throws Exception {
        requestDto = new ItemRequestDto(itemId, "item", "", true, null);

        when(itemClient.createItem(userId, requestDto)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_whenItemValid_thenResponseIsOk() throws Exception {
        final String itemName = "newItem";
        requestDto = new ItemRequestDto(itemId, itemName, "description", true, null);

        when(itemClient.updateItem(userId, itemId, requestDto)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_whenItemFound_thenResponseIsOk() throws Exception {
        when(itemClient.getItemById(userId, itemId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItems_whenItemFound_thenResponseIsOk() throws Exception {
        when(itemClient.getAllItems(userId)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_whenItemFound_thenResponseIsOk() throws Exception {
        final String text = "searchItem";

        when(itemClient.searchItems(userId, text)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(get("/items/search?text={text}", text)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_whenCommentValid_thenResponseIsOk() throws Exception {
        CommentRequestDto comment = new CommentRequestDto("comment");

        when(itemClient.addComment(userId, itemId, comment)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/items/{itemId}/comment", itemId, comment)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
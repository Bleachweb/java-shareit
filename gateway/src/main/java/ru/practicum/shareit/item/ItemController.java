package ru.practicum.shareit.item;

import jakarta.validation.Valid;
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
@RequestMapping(path = "/items")
public class ItemController {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(X_SHARER_USER_ID) int userId,
                                             @Valid @RequestBody ItemRequestDto requestDto) {

        log.info("Добавление вещи: {}, пользователем с id: {}", requestDto, userId);
        return itemClient.createItem(userId, requestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(X_SHARER_USER_ID) int userId,
                                             @PathVariable int itemId,
                                             @RequestBody ItemRequestDto requestDto) {
        log.info("Обновление вещи: {}, пользователем с id: {}", requestDto, userId);
        return itemClient.updateItem(userId, itemId, requestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(X_SHARER_USER_ID) int userId,
                                              @PathVariable int itemId) {
        log.info("Получение вещи по id: {}, пользователем с id: {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItems(@RequestHeader(X_SHARER_USER_ID) int userId) {
        log.info("Получение списка всех вещей пользователем с id: {}", userId);
        return itemClient.getAllItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader(X_SHARER_USER_ID) int userId,
                                              @RequestParam String text) {
        log.info("Поиск вещи по запросу: {}, пользователем с id: {}", text, userId);
        return itemClient.searchItems(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(X_SHARER_USER_ID) int userId,
                                             @PathVariable int itemId,
                                             @Valid @RequestBody CommentRequestDto requestDto) {
        log.info("Добавление комментария к вещи с id: {}", itemId);
        return itemClient.addComment(userId, itemId, requestDto);
    }
}
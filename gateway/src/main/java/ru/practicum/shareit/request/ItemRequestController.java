package ru.practicum.shareit.request;

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
@RequestMapping(path = "/requests")
public class ItemRequestController {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(X_SHARER_USER_ID) int userId,
                                                @Valid @RequestBody ItemRequestDto request) {
        log.info("Добавление запроса: {}", request);
        return itemRequestClient.createRequest(userId, request);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(X_SHARER_USER_ID) int userId,
                                                 @PathVariable int requestId) {
        log.info("Получение запроса по id: {}", requestId);
        return itemRequestClient.getRequestById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestByUserId(@RequestHeader(X_SHARER_USER_ID) int userId) {
        log.info("Получение списка всех запросов пользователя с id: {}", userId);
        return itemRequestClient.getAllRequestByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequest(@RequestHeader(X_SHARER_USER_ID) int userId) {
        log.info("Получение всех запросов");
        return itemRequestClient.getAllRequest();
    }
}
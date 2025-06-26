package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    private final ItemRequestService service;

    @PostMapping
    public ItemRequestDto createRequest(@RequestHeader(X_SHARER_USER_ID) int userId,
                                        @RequestBody ItemRequestDtoRequest request) {
        ItemRequestDto createdRequest = service.createRequest(userId, request);
        log.info("Добавлен запрос: {}", createdRequest);
        return createdRequest;
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(X_SHARER_USER_ID) int userId,
                                         @PathVariable int requestId) {
        ItemRequestDto request = service.getRequestById(userId, requestId);
        log.info("Получен запрос: {}", request);
        return request;
    }

    @GetMapping
    public List<ItemRequestDto> getAllRequestByUserId(@RequestHeader(X_SHARER_USER_ID) int userId) {
        List<ItemRequestDto> requests = service.getAllRequestByUserId(userId);
        log.info("Получен список запросов пользователя: {}", requests);
        return requests;
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequest(@RequestHeader(X_SHARER_USER_ID) int userId) {
        List<ItemRequestDto> requests = service.getAllRequest();
        log.info("Получен список всех запросов: {}", requests);
        return requests;
    }
}
package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Constants;
import ru.practicum.shareit.comment.CommentDtoRequest;
import ru.practicum.shareit.comment.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    public static final String X_SHARER_USER_ID = Constants.X_SHARER_USER_ID;

    private final ItemService itemService;

    @PostMapping
    public ItemDtoResponse createItem(@RequestHeader(X_SHARER_USER_ID) int userId,
                                      @RequestBody ItemDtoRequest itemRequest) {
        ItemDtoResponse createdItem = itemService.createItem(userId, itemRequest);
        log.info("Добавлена новая вещь: {}", createdItem);
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse updateItem(@RequestHeader(X_SHARER_USER_ID) int userId,
                                      @PathVariable int itemId,
                                      @RequestBody ItemDtoRequest itemRequest) {
        ItemDtoResponse itemResponse = itemService.updateItem(userId, itemId, itemRequest);
        log.info("Обновленная вещь: {}", itemResponse);
        return itemResponse;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(X_SHARER_USER_ID) int userId,
                               @PathVariable int itemId) {
        ItemDto gettingItemDto = itemService.getItemById(userId, itemId);
        log.info("Получена вещь: {}", gettingItemDto);
        return gettingItemDto;
    }

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(X_SHARER_USER_ID) int userId) {
        log.info("Получаем все вещи");
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> searchItems(@RequestHeader(X_SHARER_USER_ID) int userId,
                                             @RequestParam String text) {
        log.info("Поиск вещи по тексту {}", text);
        return itemService.searchItems(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse addComment(@RequestHeader(X_SHARER_USER_ID) int userId,
                                         @PathVariable int itemId,
                                         @RequestBody CommentDtoRequest commentRequest) {
        CommentDtoResponse comment = itemService.addComment(userId, itemId, commentRequest);
        log.info("Добавлен комментарий {} к предмету с id: {}", comment, itemId);
        return comment;
    }
}
package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(X_SHARER_USER_ID) int userId,
                              @Valid @RequestBody ItemDto itemDto) {
        ItemDto createdItemDto = itemService.createItem(userId, itemDto);
        log.info("Добавлена новая вещь: {}", createdItemDto);
        return createdItemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(X_SHARER_USER_ID) int userId,
                              @PathVariable int itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Обновляем вещь с id: {}", itemId);
        ItemDto updatingItemDto = itemService.updateItem(userId, itemId, itemDto);
        log.info("Обновленная вещь: {}", updatingItemDto);
        return updatingItemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(X_SHARER_USER_ID) int userId,
                               @PathVariable int itemId) {
        log.info("Получаем вещь по id: {}", itemId);
        ItemDto gettingItemDto = itemService.getItemById(userId, itemId);
        log.info("Получена вещь: {}", gettingItemDto);
        return gettingItemDto;
    }

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(X_SHARER_USER_ID) int userId) {
        log.info("Получаем все вещи");
        List<ItemDto> allItemsDto = itemService.getItems(userId);
        return allItemsDto;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader(X_SHARER_USER_ID) int userId,
                                     @RequestParam String text) {
        log.info("Поиск вещи по тексту {}", text);
        List<ItemDto> searchItems = itemService.searchItems(userId, text);
        log.info("Результат поиска {}", searchItems);
        return searchItems;
    }
}
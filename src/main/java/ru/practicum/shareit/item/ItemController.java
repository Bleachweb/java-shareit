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

    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") int userId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info("Добавляем новую вещь: {}", itemDto);
        ItemDto createdItemDto = itemService.createItem(userId, itemDto);
        log.info("Новая вещь добавлена: {}", createdItemDto);
        return createdItemDto;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") int userId,
                              @PathVariable int itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Обновляем вещь: {}", itemDto);
        ItemDto updatingItemDto = itemService.updateItem(userId, itemId, itemDto);
        log.info("Обновленная вещь: {}", updatingItemDto);
        return updatingItemDto;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader("X-Sharer-User-Id") int userId,
                               @PathVariable int itemId) {
        log.info("Получаем вещь по id: {}", itemId);
        ItemDto gettingItemDto = itemService.getItemById(userId, itemId);
        log.info("Получена вещь с id: {}", gettingItemDto);
        return gettingItemDto;
    }

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("Получаем все вещи");
        List<ItemDto> allItemsDto = itemService.getItems(userId);
        log.info("Все вещи получены");
        return allItemsDto;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader("X-Sharer-User-Id") int userId,
                                     @RequestParam String text) {
        log.info("Поиск вещи по тексту {}", text);
        List<ItemDto> searchItems = itemService.searchItems(userId, text);
        log.info("Результат поиска {}", searchItems);
        return searchItems;
    }
}
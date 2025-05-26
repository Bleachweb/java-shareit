package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(int userId, ItemDto itemDto);

    ItemDto updateItem(int userId, ItemDto itemDto);

    ItemDto getItemById(int userId, int itemId);

    List<ItemDto> getItems(int userId);

    List<ItemDto> searchItems(int userId, String text);

}
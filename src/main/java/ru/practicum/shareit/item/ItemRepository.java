package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item addItem(Item item);

    Item updateItem(Item item);

    Optional<Item> getItemById(int itemId);

    List<Item> getItems(int userId);

    List<Item> searchItems(int userId, String text);
}
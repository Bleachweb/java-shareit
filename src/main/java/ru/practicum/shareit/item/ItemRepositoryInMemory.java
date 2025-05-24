package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ItemRepositoryInMemory implements ItemRepository {

    private final Map<Integer, Item> items = new HashMap<>();

    @Override
    public Item addItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(int itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> getItems(int userId) {
        return items.values().stream().filter(item -> item.getOwnerId() == userId).toList();
    }

    @Override
    public List<Item> searchItems(int userId, String text) {
        return items.values()
                .stream()
                .filter(Item::isAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .toList();
    }
}
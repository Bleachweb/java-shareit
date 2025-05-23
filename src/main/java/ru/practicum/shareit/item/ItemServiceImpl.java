package ru.practicum.shareit.item;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private Integer itemId = 0;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(int userId, ItemDto itemDto) {

        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        int id = getId();
        Item createdItem = ItemMapper.dtoToItem(itemDto);
        createdItem.setId(id);
        createdItem.setOwnerId(userId);

        createdItem = itemRepository.addItem(createdItem);

        return ItemMapper.itemToDto(createdItem);
    }

    @Override
    public ItemDto updateItem(int userId, int itemId, ItemDto itemDto) {

        userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item oldItem = itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (oldItem.getOwnerId() != userId) {
            log.error("Пользователь с id {} не является владельцем вещи с id {}", userId, itemId);
            throw new ValidationException("Пользователь не является владельцем вещи");
        }
        if (itemDto.getName() == null || itemDto.getName().isEmpty()) {
            itemDto.setName(oldItem.getName());
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isEmpty()) {
            itemDto.setDescription(oldItem.getDescription());
        }
        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(false);
        }

        itemDto.setId(itemId);
        itemDto.setOwnerId(userId);

        Item updatedItem = itemRepository.updateItem(ItemMapper.dtoToItem(itemDto));

        return ItemMapper.itemToDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(int userId, int itemId) {
        return ItemMapper.itemToDto(itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена")));
    }

    @Override
    public List<ItemDto> getItems(int userId) {
        return ItemMapper.itemsToDto(itemRepository.getItems(userId));
    }

    @Override
    public List<ItemDto> searchItems(int userId, String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return ItemMapper.itemsToDto(itemRepository.searchItems(userId, text.toLowerCase()));
    }

    private Integer getId() {
        itemId++;
        return itemId;
    }
}
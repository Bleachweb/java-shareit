package ru.practicum.shareit.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.comment.CommentDtoRequest;
import ru.practicum.shareit.comment.CommentDtoResponse;
import ru.practicum.shareit.item.dto.CreatedItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.List;

@Transactional(readOnly = true)
public interface ItemService {

    @Transactional
    CreatedItemDtoResponse createItem(int userId, ItemDtoRequest itemRequestDto);

    @Transactional
    ItemDtoResponse updateItem(int userId, int itemId, ItemDtoRequest itemRequestDto);

    ItemDto getItemById(int userId, int itemId);

    List<ItemDto> getItems(int userId);

    List<ItemDtoResponse> searchItems(int userId, String text);

    @Transactional
    CommentDtoResponse addComment(int userId, int itemId, CommentDtoRequest commentDtoRequest);
}
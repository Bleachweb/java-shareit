package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.comment.CommentDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

@Component
public class ItemMapper {

    public Item dtoToItem(ItemDtoRequest itemDtoRequest, User user, Integer itemId) {
        return Item.builder()
                .id(itemId)
                .name(itemDtoRequest.getName())
                .description(itemDtoRequest.getDescription())
                .available(itemDtoRequest.getAvailable())
                .user(user)
                .build();
    }

    public ItemDtoResponse itemToDtoResponse(Item item) {
        return ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .owner(item.getUser())
                .available(item.isAvailable())
                .build();
    }

    public CreatedItemDtoResponse createdItemDtoResponse(Item item) {
        return CreatedItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public ItemDto toItemDto(int userId, ItemDtoResponse item, List<CommentDtoResponse> comments,
                             BookingDtoForItem nextBooking, BookingDtoForItem lastBooking) {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .owner(item.getOwner())
                .available(item.getAvailable())
                .build();

        if (comments != null) {
            itemDto.setComments(comments);
        }

        if (item.getOwner().getId() == userId) {
            if (nextBooking != null) {
                itemDto.setNextBooking(nextBooking);
            }
            if (lastBooking != null) {
                itemDto.setLastBooking(lastBooking);
            }
        }
        return itemDto;
    }
}
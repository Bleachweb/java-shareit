package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.user.User;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest itemRequest, List<ItemDtoResponse> items) {

        String createdDate = DateTimeFormatter
                .ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(itemRequest.getCreated());

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requester(itemRequest.getRequester())
                .created(createdDate)
                .items(items)
                .build();
    }

    public ItemRequest toRequest(User user, ItemRequestDtoRequest requestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(requestDto.getDescription());
        itemRequest.setRequester(user);
        return itemRequest;
    }

    public List<ItemRequestDto> requestsToDto(List<ItemRequest> itemRequests, List<ItemDtoResponse> items) {
        return itemRequests.stream()
                .map(request -> toDto(request, items))
                .collect(Collectors.toList());
    }
}
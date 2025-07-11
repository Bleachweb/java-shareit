package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper mapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto createRequest(int userId, ItemRequestDtoRequest requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id - " + userId + " не найден!"));
        ItemRequest itemRequest = itemRequestRepository.save(mapper.toRequest(user, requestDto));
        return mapper.toDto(itemRequest, null);
    }

    @Override
    public ItemRequestDto getRequestById(int userId, int requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id - " + requestId + " не найден!"));

        List<ItemDtoResponse> items = itemRepository.findAllItemsByItemRequestIdOrderByIdAsc(requestId)
                .stream()
                .map(itemMapper::itemToDtoResponse)
                .collect(Collectors.toList());

        return mapper.toDto(itemRequest, items);
    }

    @Override
    public List<ItemRequestDto> getAllRequestByUserId(int userId) {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdWithUser(userId);
        if (itemRequests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> requestIds = itemRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Integer, List<ItemDtoResponse>> itemsByRequestId = items.stream()
                .map(itemMapper::itemToDtoResponse)
                .collect(Collectors.groupingBy(ItemDtoResponse::getRequestId));

        return itemRequests.stream()
                .map(request -> {
                    List<ItemDtoResponse> itemsForRequest =
                            itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
                    return mapper.toDto(request, itemsForRequest);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequest() {
        List<ItemRequest> itemRequests = itemRequestRepository.findAll();
        List<ItemDtoResponse> items = itemRequests
                .stream()
                .flatMap(request -> itemRepository.findAllItemsByUserIdOrderByIdAsc(
                                request.getRequester().getId()
                        )
                        .stream())
                .map(itemMapper::itemToDtoResponse)
                .collect(Collectors.toList());

        return mapper.requestsToDto(itemRequests, items);
    }
}
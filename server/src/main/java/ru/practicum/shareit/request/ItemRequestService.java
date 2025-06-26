package ru.practicum.shareit.request;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ItemRequestService {

    @Transactional
    ItemRequestDto createRequest(int userId, ItemRequestDtoRequest requestDto);

    ItemRequestDto getRequestById(int userId, int requestId);

    List<ItemRequestDto> getAllRequestByUserId(int userId);

    List<ItemRequestDto> getAllRequest();
}
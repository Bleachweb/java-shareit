package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoResponse {

    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
}
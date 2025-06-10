package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoRequest {

    @NotBlank(message = "Название должно быть указано")
    private String name;

    @NotBlank(message = "Описание должно быть указано")
    private String description;

    @NotNull(message = "Статус аренды должен быть указан")
    private Boolean available;
}
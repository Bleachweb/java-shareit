package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private int itemId;

    @NotBlank(message = "Название должно быть указано!")
    private String name;

    @NotBlank(message = "Описание должно быть указано!")
    private String description;

    @NotNull(message = "Статус аренды должен быть указан!")
    private Boolean available;

    private Integer requestId;
}
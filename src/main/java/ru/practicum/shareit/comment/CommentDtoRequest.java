package ru.practicum.shareit.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoRequest {

    @NotBlank(message = "Комментарий не может быть пустым")
    @Size(min = 10, max = 500, message = "Комментарий должен быть от 10 до 500 символов")
    private String text;
}
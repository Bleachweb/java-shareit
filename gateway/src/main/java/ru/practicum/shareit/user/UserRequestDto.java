package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    private int userId;

    @NotBlank(message = "Имя не может быть пустым!")
    @Size(min = 4, max = 50, message = "Имя должно быть от 4 до 50 символов!")
    private String name;

    @NotBlank(message = "Email не может быть пустым!")
    @Email(message = "Неверный формат почты!")
    private String email;
}
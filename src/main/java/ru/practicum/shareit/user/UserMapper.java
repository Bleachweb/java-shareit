package ru.practicum.shareit.user;


import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public static UserDto userToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User dtoToUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static List<UserDto> usersToDto(List<User> users) {
        return users.stream().map(UserMapper::userToDto).toList();
    }
}
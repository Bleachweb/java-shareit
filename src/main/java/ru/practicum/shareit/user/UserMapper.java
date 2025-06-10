package ru.practicum.shareit.user;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUser(UserDto userDto) {
        return new User(userDto.getId(), userDto.getName(), userDto.getEmail());
    }

    public List<UserDto> usersToDto(List<User> users) {
        return users.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }
}
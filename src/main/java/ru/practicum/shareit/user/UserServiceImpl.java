package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userRepository.addUser(UserMapper.dtoToUser(userDto));
        return UserMapper.userToDto(user);
    }

    @Override
    public UserDto updateUser(int userId, UserDto userDto) {

        User oldUser = userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        User updatedUser = UserMapper.dtoToUser(userDto);
        updatedUser.setId(userId);

        if (userDto.getName() == null) {
            updatedUser.setName(oldUser.getName());
        }
        if (userDto.getEmail() == null) {
            updatedUser.setEmail(oldUser.getEmail());
        }

        User user = userRepository.updateUser(updatedUser);
        return UserMapper.userToDto(user);
    }

    @Override
    public UserDto getUserById(int userId) {
        User user = userRepository.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return UserMapper.userToDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        List<User> users = userRepository.getUsers();
        return UserMapper.usersToDto(users);
    }

    @Override
    public void deleteUser(int userId) {
        userRepository.deleteUser(userId);
    }
}
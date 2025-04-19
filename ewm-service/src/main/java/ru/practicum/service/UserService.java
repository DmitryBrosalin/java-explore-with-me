package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.model.dto.UserDto;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto addUser(UserDto userDto) {
        User user = UserMapper.fromUserDto(userDto);
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictException("email: " + userDto.getEmail() + " already used.");
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    public List<UserDto> getUsers(List<Integer> ids, int from, int size) {
        if (ids == null) {
            return userRepository.findUsers(from, size).stream().map(UserMapper::toUserDto).toList();
        } else {
            return userRepository.findUsersByIds(ids, from, size).stream().map(UserMapper::toUserDto).toList();
        }
    }

    public User findUser(int userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        } else {
            return userRepository.findById(userId).get();
        }
    }

    public void deleteUser(int userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        userRepository.deleteById(userId);
    }
}

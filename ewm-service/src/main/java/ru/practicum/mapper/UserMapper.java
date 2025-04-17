package ru.practicum.mapper;

import ru.practicum.model.User;
import ru.practicum.model.dto.UserDto;
import ru.practicum.model.dto.UserShortDto;

public class UserMapper {
    public static User fromUserDto(UserDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}

package ru.practicum.mapper;

import ru.practicum.model.Request;
import ru.practicum.model.dto.RequestDto;

import java.time.format.DateTimeFormatter;

public class RequestMapper {
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static RequestDto toRequestDto(Request request) {
        return RequestDto.builder()
                .id(request.getId())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(String.valueOf(request.getStatus()))
                .created(request.getCreated().format(FORMATTER))
                .build();
    }
}

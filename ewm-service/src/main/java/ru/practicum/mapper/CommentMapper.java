package ru.practicum.mapper;

import ru.practicum.model.Comment;
import ru.practicum.model.dto.CommentResponseDto;
import ru.practicum.model.dto.CommentRequestDto;

import java.time.format.DateTimeFormatter;

public class CommentMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Comment fromCommentRequestDto(CommentRequestDto commentRequestDto) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .build();
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated().format(FORMATTER))
                .build();
    }
}

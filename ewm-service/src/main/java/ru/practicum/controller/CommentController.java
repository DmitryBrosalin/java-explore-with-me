package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.CommentRequestDto;
import ru.practicum.model.dto.CommentResponseDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/comments/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto postComment(@PathVariable int userId,
                                          @PathVariable int eventId,
                                          @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return commentService.addComment(userId, eventId, commentRequestDto);
    }

    @PatchMapping("/users/{userId}/comments/{eventId}/{commentId}")
    public CommentResponseDto patchComment(@PathVariable int userId,
                                           @PathVariable int eventId,
                                           @PathVariable int commentId,
                                           @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return commentService.updateComment(userId, eventId, commentId, commentRequestDto);
    }

    @GetMapping("/users/{userId}/comments/{eventId}/{commentId}")
    public CommentResponseDto getComment(@PathVariable int userId,
                                         @PathVariable int eventId,
                                         @PathVariable int commentId) {
        return commentService.getComment(userId, eventId, commentId);
    }

    @DeleteMapping("/users/{userId}/comments/{eventId}/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable int userId,
                              @PathVariable int eventId,
                              @PathVariable int commentId) {
        commentService.deleteComment(userId, eventId, commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentResponseDto> getAllCommentsForEvent(@PathVariable int eventId) {
        return commentService.getAllCommentsForEvent(eventId);
    }

    @GetMapping("/admin/users/{userId}/comments")
    public List<CommentResponseDto> getAllCommentsForUserByAdmin(@PathVariable int userId) {
        return commentService.getAllCommentsForUserByAdmin(userId);
    }

    @DeleteMapping("/admin/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable int userId,
                                     @PathVariable int commentId) {
        commentService.deleteCommentByAdmin(userId, commentId);
    }
}

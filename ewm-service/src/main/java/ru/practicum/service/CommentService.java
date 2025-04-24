package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.State;
import ru.practicum.model.User;
import ru.practicum.model.dto.CommentRequestDto;
import ru.practicum.model.dto.CommentResponseDto;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    public CommentResponseDto addComment(int userId, int eventId, CommentRequestDto commentRequestDto) {
        User user = userService.findUser(userId);
        Event event = findEvent(eventId);
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        Comment comment = CommentMapper.fromCommentRequestDto(commentRequestDto);
        comment.setUser(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        return prepareForResponse(commentRepository.save(comment));
    }

    private CommentResponseDto prepareForResponse(Comment comment) {
        CommentResponseDto response = CommentMapper.toCommentResponseDto(comment);
        response.setAuthorName(comment.getUser().getName());
        return response;
    }

    public Comment findComment(int id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new NotFoundException("Comment with id=" + id + " was not found.");
        }
    }

    public CommentResponseDto updateComment(int userId, int eventId, int commentId, CommentRequestDto commentRequestDto) {
        Comment oldComment = findComment(commentId);
        if (userId != oldComment.getUser().getId()) {
            throw new BadRequestException("Only author can update comment.");
        }
        if (eventId != oldComment.getEvent().getId()) {
            throw new NotFoundException("No comment with id=" + commentId + " for event with id=" + eventId);
        }
        oldComment.setText(commentRequestDto.getText());
        return prepareForResponse(commentRepository.save(oldComment));
    }

    public CommentResponseDto getComment(int userId, int eventId, int commentId) {
        Comment comment = findComment(commentId);
        if (userId != comment.getUser().getId()) {
            throw new BadRequestException("Only author can get comment using this endpoint.");
        }
        if (eventId != comment.getEvent().getId()) {
            throw new NotFoundException("No comment with id=" + commentId + " for event with id=" + eventId);
        }
        return prepareForResponse(comment);
    }


    public void deleteComment(int userId, int eventId, int commentId) {
        Comment comment = findComment(commentId);
        if (userId != comment.getUser().getId()) {
            throw new BadRequestException("Only author can delete comment.");
        }
        if (eventId != comment.getEvent().getId()) {
            throw new NotFoundException("No comment with id=" + commentId + " for event with id=" + eventId);
        }
        commentRepository.deleteById(commentId);
    }

    public List<CommentResponseDto> getAllCommentsForEvent(int eventId) {
        findEvent(eventId);
        List<Comment> comments = commentRepository.findByEventId(eventId);
        return comments.stream().map(this::prepareForResponse).toList();
    }

    public List<CommentResponseDto> getAllCommentsForUserByAdmin(int userId) {
        userService.findUser(userId);
        List<Comment> comments = commentRepository.findByUserId(userId);
        return comments.stream().map(this::prepareForResponse).toList();
    }

    public void deleteCommentByAdmin(int userId, int commentId) {
        Comment comment = findComment(commentId);
        if (userId != comment.getUser().getId()) {
            throw new NotFoundException("No comment with id=" + commentId + " by user with id=" + userId);
        }
        commentRepository.deleteById(commentId);
    }

    private Event findEvent(int id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()) {
            return event.get();
        } else {
            throw new NotFoundException("Event with id=" + id + " was not found");
        }
    }
}

package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.*;
import ru.practicum.model.dto.EventRequestStatusUpdateRequest;
import ru.practicum.model.dto.EventRequestStatusUpdateResult;
import ru.practicum.model.dto.RequestDto;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final EventService eventService;
    private final UserService userService;
    private final RequestRepository requestRepository;

    public RequestDto addRequest(int userId, int eventId) {
        Event event = eventService.findEvent(eventId);
        User requester = userService.findUser(userId);
        if (requestRepository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Request already exists");
        }
        if (event.getInitiator().getId() == requester.getId()) {
            throw new ConflictException("Initiator cannot request his own event");
        }
        if (!(event.getState().equals(State.PUBLISHED))) {
            throw new ConflictException("This event is not published");
        }
        if (event.getParticipantLimit() > 0
                && (long) event.getParticipantLimit() == requestRepository.countConfirmedRequestsForEvent(eventId)) {
            throw new ConflictException("Participant Limit was reached");
        }
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .build();
        if (event.getRequestModeration()) {
            request.setStatus(Status.PENDING);
        } else {
            request.setStatus(Status.CONFIRMED);
        }
        if (event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        }
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    public RequestDto cancelRequest(int userId, int requestId) {
        if (requestRepository.findById(requestId).isEmpty()) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }
        Request request = requestRepository.findById(requestId).get();
        if (request.getStatus().equals(Status.CONFIRMED)) {
            throw new ConflictException("Request cannot be canceled, it is: " + request.getStatus());
        }
        request.setStatus(Status.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    public List<RequestDto> getRequests(int userId) {
        userService.findUser(userId);
        List<Request> requests = requestRepository.findByRequesterId(userId);
        return requests.stream().map(RequestMapper::toRequestDto).toList();
    }

    private Request findRequest(int requestId) {
        if (requestRepository.findById(requestId).isEmpty()) {
            throw new NotFoundException("Request with id=" + requestId + " was not found");
        }
        return requestRepository.findById(requestId).get();
    }

    private void checkParticipantLimit(Request request) {
        if (request.getEvent().getParticipantLimit() ==
                requestRepository.countConfirmedRequestsForEvent(request.getEvent().getId())) {
            throw new ConflictException("Participant Limit was reached");
        }
    }

    public EventRequestStatusUpdateResult updateRequestStatusByInitiator(int userId,
                                          EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Status status = eventRequestStatusUpdateRequest.getStatus();
        List<RequestDto> result = eventRequestStatusUpdateRequest.getRequestIds().stream()
                .map(this::findRequest)
                .peek(request -> {
                    checkParticipantLimit(request);
                    request.setStatus(status);
                    requestRepository.save(request);
                })
                .map(RequestMapper::toRequestDto)
                .toList();
        if (status.equals(Status.CONFIRMED)) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(result)
                    .build();
        } else if (status.equals(Status.REJECTED)) {
            return EventRequestStatusUpdateResult.builder()
                    .rejectedRequests(result)
                    .build();
        } else {
            throw new BadRequestException("Cannot update request status: " + status);
        }
    }

    public List<RequestDto> getRequestsForEvent(int userId, int eventId) {
        eventService.findEvent(eventId);
        userService.findUser(userId);
        return requestRepository.findByEventId(eventId).stream().map(RequestMapper::toRequestDto).toList();
    }
}

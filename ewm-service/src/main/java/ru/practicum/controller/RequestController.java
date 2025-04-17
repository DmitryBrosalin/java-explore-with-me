package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.RequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @GetMapping
    public List<RequestDto> getRequests(@PathVariable int userId) {
        return requestService.getRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestDto postRequest(@PathVariable int userId,
                                  @RequestParam int eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @PatchMapping(value = "/{requestId}/cancel")
    public RequestDto cancelRequest(@PathVariable int userId,
                                    @PathVariable int requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}

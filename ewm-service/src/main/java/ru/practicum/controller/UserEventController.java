package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.*;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class UserEventController {
    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public List<EventShortDto> getEventsForUser(@PathVariable int userId,
                                                @RequestParam(required = false, defaultValue = "0") int from,
                                                @RequestParam(required = false, defaultValue = "10") int size) {
        return eventService.getEventsForUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto postEvent(@PathVariable int userId,
                                  @RequestBody NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping(value = "/{eventId}")
    public EventFullDto getEvent(@PathVariable int userId,
                                 @PathVariable int eventId) {
        return eventService.getEventForUser(userId, eventId);
    }

    @PatchMapping(value = "/{eventId}")
    public EventFullDto patchEventByUser(@PathVariable int userId,
                               @PathVariable int eventId,
                               @RequestBody UpdateEventRequest updateEventRequest) {
        return eventService.updateEventByUser(userId, eventId, updateEventRequest);
    }

    @GetMapping(value = "/{eventId}/requests")
    public List<RequestDto> getRequestsForEvent(@PathVariable int userId,
                                                @PathVariable int eventId) {
        return requestService.getRequestsForEvent(userId, eventId);
    }

    @PatchMapping(value = "/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatusByInitiator(@PathVariable int userId,
                                         @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return requestService.updateRequestStatusByInitiator(userId, eventRequestStatusUpdateRequest);
    }

}

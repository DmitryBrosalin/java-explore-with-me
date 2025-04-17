package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.EventFullDto;
import ru.practicum.model.dto.EventShortDto;
import ru.practicum.model.dto.UpdateEventRequest;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {

private static final String APP_NAME = "ewm-main-service";

    private final EventService eventService;

    @GetMapping("/admin/events")
    public List<EventFullDto> getEventsByAdmin(@RequestParam(required = false) List<Integer> users,
                                           @RequestParam(required = false) List<String> states,
                                           @RequestParam(required = false) List<Integer> categories,
                                           @RequestParam(required = false) String rangeStart,
                                           @RequestParam(required = false) String rangeEnd,
                                           @RequestParam(required = false, defaultValue = "0") int from,
                                           @RequestParam(required = false, defaultValue = "10") int size) {
        return eventService.getEventsByAdmin(users,
                                            states,
                                            categories,
                                            rangeStart,
                                            rangeEnd,
                                            from,
                                            size);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto patchEventByAdmin(@PathVariable int eventId,
                                      @RequestBody UpdateEventRequest updateEventRequest) {
        return eventService.updateEventByAdmin(eventId, updateEventRequest);
    }

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Integer> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                         @RequestParam(required = false, defaultValue = "0") int from,
                                         @RequestParam(required = false, defaultValue = "10") int size,
                                         HttpServletRequest request) {
        return eventService.getEvents(text,
                                    categories,
                                    paid,
                                    rangeStart,
                                    rangeEnd,
                                    onlyAvailable,
                                    sort,
                                    from,
                                    size,
                                    request,
                                    APP_NAME);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getEvent(@PathVariable int eventId,
                                 HttpServletRequest request) {
        return eventService.getEvent(eventId, request, APP_NAME);
    }
}

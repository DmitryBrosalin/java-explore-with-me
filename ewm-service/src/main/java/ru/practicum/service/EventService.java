package ru.practicum.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.InternalError;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.*;
import ru.practicum.model.dto.EventFullDto;
import ru.practicum.model.dto.EventShortDto;
import ru.practicum.model.dto.NewEventDto;
import ru.practicum.model.dto.UpdateEventRequest;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.StatsClient;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;
    private final EventSpecification eventSpecification;

    public EventFullDto addEvent(int userId, NewEventDto newEventDto) {
        validateNewEvent(newEventDto);
        User initiator = userService.findUser(userId);
        if (newEventDto.getCategory() == 0) {
            throw new BadRequestException("Field: category. Error: must not be blank. Value: " + 0);
        }
        Category category = categoryService.findCategory(newEventDto.getCategory());
        Event event = EventMapper.fromNewEventDto(newEventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("EventDate cannot be in past.");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Field: eventDate. Error: должно содержать дату, которая не раньше, чем через 2 часа." +
                    "Value: " + event.getEventDate().format(FORMATTER));
        }
        Location location = LocationMapper.fromLocationDto(newEventDto.getLocation());
        location = locationRepository.save(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    private void validateNewEvent(NewEventDto newEventDto) {
        if (newEventDto.getDescription() == null || newEventDto.getDescription().isBlank()
                || newEventDto.getAnnotation() == null || newEventDto.getAnnotation().isBlank()
                || newEventDto.getParticipantLimit() < 0) {
            throw new BadRequestException("Incorrect request body to create new Event");
        }
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        if (newEventDto.getDescription().length() < 20
                || newEventDto.getDescription().length() > 7000) {
            throw new BadRequestException("Description length must be between 20 and 7000. It is: " +
                    newEventDto.getDescription().length());
        }
        if (newEventDto.getAnnotation().length() < 20
                || newEventDto.getAnnotation().length() > 2000) {
            throw new BadRequestException("Annotation length must be between 20 and 2000. It is: " +
                    newEventDto.getAnnotation().length());
        }
        if (newEventDto.getTitle().length() < 3
                || newEventDto.getTitle().length() > 120) {
            throw new BadRequestException("Title length must be between 3 and 120. It is: " +
                    newEventDto.getTitle().length());
        }
    }

    public EventFullDto getEvent(int eventId, HttpServletRequest request, String app) {
        Event event = findEvent(eventId);
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        statsClient.postHit(HitDto.builder()
                .app(app)
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(findViewsForEvents(List.of(event)).get(eventId));
        eventFullDto.setConfirmedRequests(requestRepository.countConfirmedRequestsForEvent(eventId));
        return eventFullDto;
    }

    public Event findEvent(int id) {
        Optional<Event> event = eventRepository.findById(id);
        if (event.isPresent()) {
            return event.get();
        } else {
            throw new NotFoundException("Event with id=" + id + " was not found");
        }
    }

    private Map<Integer, Long> findViewsForEvents(List<Event> events) {
        String uriTemplate = "/events/";
        LocalDateTime start = events.stream().map(Event::getCreatedOn).min(LocalDateTime::compareTo).get();
        List<String> uris = events.stream().map(event -> uriTemplate + event.getId()).toList();
        Object response = statsClient.getStats(start.format(FORMATTER),
                        LocalDateTime.now().format(FORMATTER),
                        uris,
                        true)
                .getBody();
        if (!(response instanceof List<?> responseList)) {
            throw new InternalError("Error in servers' interactions. Unknown response Class: " + response.getClass());
        }
        List<StatsDto> stats = responseList.stream().filter(responseElement -> responseElement instanceof Map)
                .map(responseElement -> objectMapper.convertValue(responseElement, StatsDto.class))
                .filter(stat -> stat.getUri().startsWith(uriTemplate))
                .toList();
        Map<Integer, Long> result = new HashMap<>();
        for (StatsDto stat: stats) {
            try {
                Integer id = Integer.parseInt(stat.getUri().substring(stat.getUri().lastIndexOf('/') + 1));
                result.put(id, stat.getHits());
            } catch (NumberFormatException e) {
                throw new InternalError("Error in parsing event id while counting views for event");
            }
        }
        for (Event event: events) {
            if (!result.containsKey(event.getId())) {
                result.put(event.getId(), 0L);
            }
        }
        return result;
    }

    public EventFullDto getEventForUser(int userId, int eventId) {
        userService.findUser(userId);
        Event event = findEvent(eventId);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setViews(findViewsForEvents(List.of(event)).get(eventId));
        eventFullDto.setConfirmedRequests(requestRepository.countConfirmedRequestsForEvent(eventId));
        return eventFullDto;
    }

    public EventFullDto updateEventByAdmin(int eventId, UpdateEventRequest updateEventRequest) {
        Event oldEvent = findEvent(eventId);
        updateEvent(oldEvent, updateEventRequest);
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateEventRequest.getEventDate(), FORMATTER);
            if (eventDate.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("EventDate cannot be in past.");
            }
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Field: eventDate. Error: должно содержать дату, которая не раньше, чем через 1 час." +
                        "Value: " + eventDate.format(FORMATTER));
            }
            oldEvent.setEventDate(eventDate);
        }
        if (updateEventRequest.getStateAction() == null) {
            return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
        } else if (updateEventRequest.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
            if (oldEvent.getState().equals(State.PENDING)) {
                oldEvent.setState(State.PUBLISHED);
                oldEvent.setPublishedOn(LocalDateTime.now());
                return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
            } else {
                throw new ConflictException("Cannot publish the event because it's not in the right state: " + oldEvent.getState());
            }
        } else if (updateEventRequest.getStateAction().equals(StateAction.REJECT_EVENT)) {
            oldEvent.setState(State.CANCELED);
            return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
        } else {
            throw new BadRequestException("Forbidden Admin State Action value: " + updateEventRequest.getStateAction());
        }
    }

    public EventFullDto updateEventByUser(int userId, int eventId, UpdateEventRequest updateEventRequest) {
        userService.findUser(userId);
        Event oldEvent = findEvent(eventId);
        updateEvent(oldEvent, updateEventRequest);
        if (updateEventRequest.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(updateEventRequest.getEventDate(), FORMATTER);
            if (eventDate.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("EventDate cannot be in past.");
            }
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Field: eventDate. Error: должно содержать дату, которая не раньше, чем через 2 часа." +
                        "Value: " + eventDate.format(FORMATTER));
            }
            oldEvent.setEventDate(eventDate);
        }
        if (updateEventRequest.getStateAction() == null) {
            return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
        } else if (updateEventRequest.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
            if (oldEvent.getState().equals(State.PENDING) || oldEvent.getState().equals(State.CANCELED)) {
                oldEvent.setState(State.PENDING);
                return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
            } else {
                throw new ConflictException("Cannot update the event because it's not in the right state: " + oldEvent.getState());
            }
        } else if (updateEventRequest.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
            oldEvent.setState(State.CANCELED);
            return EventMapper.toEventFullDto(eventRepository.save(oldEvent));
        } else {
            throw new BadRequestException("Forbidden User State Action value: " + updateEventRequest.getStateAction());
        }
    }

    private void updateEvent(Event oldEvent, UpdateEventRequest updateEventRequest) {
        if (oldEvent.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Cannot update event because it is: " + oldEvent.getState());
        }
        if (updateEventRequest.getParticipantLimit() < 0) {
            throw new BadRequestException("Participant Limit cannot be negative: " + updateEventRequest.getParticipantLimit());
        }
        validateUpdateFieldsLength(updateEventRequest);
        if (updateEventRequest.getCategory() != 0) {
            Category category = categoryService.findCategory(updateEventRequest.getCategory());
            oldEvent.setCategory(category);
        }
        if (updateEventRequest.getLocation() != null) {
            if (!(updateEventRequest.getLocation()
                    .equals(LocationMapper.toLocationDto(oldEvent.getLocation())))) {
                Location location = LocationMapper.fromLocationDto(updateEventRequest.getLocation());
                location = locationRepository.save(location);
                oldEvent.setLocation(location);
            }
        }
        if (updateEventRequest.getAnnotation() != null) oldEvent.setAnnotation(updateEventRequest.getAnnotation());
        if (updateEventRequest.getDescription() != null) oldEvent.setDescription(updateEventRequest.getDescription());
        if (updateEventRequest.getPaid() != null) oldEvent.setPaid(updateEventRequest.getPaid());
        if (updateEventRequest.getParticipantLimit() > 0) oldEvent.setParticipantLimit(updateEventRequest.getParticipantLimit());
        if (updateEventRequest.getRequestModeration() != null) oldEvent.setRequestModeration(updateEventRequest.getRequestModeration());
        if (updateEventRequest.getTitle() != null) oldEvent.setTitle(updateEventRequest.getTitle());
    }

    private void validateUpdateFieldsLength(UpdateEventRequest updateEventDto) {
        if (updateEventDto.getDescription() != null && (updateEventDto.getDescription().length() < 20
                || updateEventDto.getDescription().length() > 7000)) {
            throw new BadRequestException("Description length must be between 20 and 7000. It is: " +
                    updateEventDto.getDescription().length());
        }
        if (updateEventDto.getAnnotation() != null && (updateEventDto.getAnnotation().length() < 20
                || updateEventDto.getAnnotation().length() > 2000)) {
            throw new BadRequestException("Annotation length must be between 20 and 2000. It is: " +
                    updateEventDto.getAnnotation().length());
        }
        if (updateEventDto.getTitle() != null && (updateEventDto.getTitle().length() < 3
                || updateEventDto.getTitle().length() > 120)) {
            throw new BadRequestException("Title length must be between 3 and 120. It is: " +
                    updateEventDto.getTitle().length());
        }
    }

    public List<EventShortDto> getEventsForUser(int userId, int from, int size) {
        userService.findUser(userId);
        List<Event> events = eventRepository.findByInitiatorIdFromLimit(userId, from, size);
        Map<Integer, Long> views = findViewsForEvents(events);
        return events.stream().map(EventMapper::toEventShortDto)
                .peek(event -> event.setConfirmedRequests(requestRepository.countConfirmedRequestsForEvent(event.getId())))
                .peek(eventShortDto -> eventShortDto.setViews(views.get(eventShortDto.getId())))
                .toList();
    }

    public List<EventShortDto> getEvents(String text,
                                         List<Integer> categories,
                                         Boolean paid,
                                         String rangeStart,
                                         String rangeEnd,
                                         Boolean onlyAvailable,
                                         String sort,
                                         int from,
                                         int size,
                                         HttpServletRequest request,
                                         String app) {
        statsClient.postHit(HitDto.builder()
                .app(app)
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build());
        if (text != null && !text.isBlank()) {
            text = text.toLowerCase();
        }
        LocalDateTime start = LocalDateTime.now();
        if (rangeStart != null && !rangeStart.isBlank()) {
            try {
                start = LocalDateTime.parse(rangeStart, FORMATTER);
            } catch (RuntimeException e) {
                throw new BadRequestException("Cannot parse RangeStart");
            }
        }

        LocalDateTime end = LocalDateTime.now().plusYears(10);
        if (rangeEnd != null && !rangeEnd.isBlank()) {
            try {
                end = LocalDateTime.parse(rangeEnd, FORMATTER);
            } catch (RuntimeException e) {
                throw new BadRequestException("Cannot parse RangeEnd");
            }
        }

        if (!start.isBefore(end)) {
            throw new BadRequestException("RangeStart must be after RangeEnd");
        }

        if (!sort.equals(String.valueOf(Sort.EVENT_DATE)) && !sort.equals(String.valueOf(Sort.VIEWS))) {
            throw new BadRequestException("Unknown sort :" + sort);
        }

        Specification<Event> specification = eventSpecification.buildPublicSpecification(text,
                categories,
                paid,
                start,
                end);

        List<Event> events = eventRepository.findAll(specification, PageRequest.of(from, size)).getContent();
        if (onlyAvailable != null && onlyAvailable) {
            for (Event event : events) {
                long confirmedRequests = requestRepository.countConfirmedRequestsForEvent(event.getId());
                if (confirmedRequests == event.getParticipantLimit()) {
                    events.remove(event);
                }
            }
        }
        Map<Integer, Long> views = findViewsForEvents(events);
        List<EventShortDto> result = new ArrayList<>(events.stream()
                .map(EventMapper::toEventShortDto)
                .peek(event -> event.setConfirmedRequests(requestRepository.countConfirmedRequestsForEvent(event.getId())))
                .peek(eventShortDto -> eventShortDto.setViews(views.get(eventShortDto.getId())))
                .toList());

        if (sort.equals(String.valueOf(Sort.EVENT_DATE))) {
            result.sort(Comparator.comparing(EventShortDto::getViews));
        } else {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return result;
    }

    public List<EventFullDto> getEventsByAdmin(List<Integer> users,
                                               List<String> states,
                                               List<Integer> categories,
                                               String rangeStart,
                                               String rangeEnd,
                                               int from,
                                               int size) {
        LocalDateTime start = LocalDateTime.now();
        if (rangeStart != null && !rangeStart.isBlank()) {
            try {
                start = LocalDateTime.parse(rangeStart, FORMATTER);
            } catch (RuntimeException e) {
                throw new BadRequestException("Cannot parse RangeStart");
            }
        }

        LocalDateTime end = LocalDateTime.now().plusYears(10);
        if (rangeEnd != null && !rangeEnd.isBlank()) {
            try {
                end = LocalDateTime.parse(rangeEnd, FORMATTER);
            } catch (RuntimeException e) {
                throw new BadRequestException("Cannot parse RangeEnd");
            }
        }

        if (!start.isBefore(end)) {
            throw new BadRequestException("RangeStart must be after RangeEnd");
        }

        Specification<Event> specification = eventSpecification.buildAdminSpecification(users,
                                                                                        states,
                                                                                        categories,
                                                                                        start,
                                                                                        end);

        List<Event> events = eventRepository.findAll(specification, PageRequest.of(from, size)).getContent();

        Map<Integer, Long> views = findViewsForEvents(events);
        return events.stream()
                .map(EventMapper::toEventFullDto)
                .peek(event -> event.setConfirmedRequests(requestRepository.countConfirmedRequestsForEvent(event.getId())))
                .peek(eventShortDto -> eventShortDto.setViews(views.get(eventShortDto.getId())))
                .toList();
    }
}

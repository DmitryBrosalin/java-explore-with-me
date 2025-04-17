package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.CompilationEvent;
import ru.practicum.model.Event;
import ru.practicum.model.dto.CompilationRequestDto;
import ru.practicum.model.dto.CompilationResponseDto;
import ru.practicum.repository.CompilationEventRepository;
import ru.practicum.repository.CompilationRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CompilationService {
    private static final int MAX_COMPILATION_TITLE_LENGTH = 50;
    private final CompilationRepository compilationRepository;
    private final CompilationEventRepository compilationEventRepository;
    private final EventService eventService;

    public CompilationResponseDto addCompilation(CompilationRequestDto compilationRequestDto) {
        if (compilationRequestDto.getPinned() == null) {
            compilationRequestDto.setPinned(false);
        }
        validateNewCompilation(compilationRequestDto);
        Compilation compilation = CompilationMapper.fromCompilationRequestDto(compilationRequestDto);
        compilation = compilationRepository.save(compilation);
        if (compilationRequestDto.getEvents() != null && !compilationRequestDto.getEvents().isEmpty()) {
            saveCompilationEvents(compilation, compilationRequestDto.getEvents());
        }
        return prepareForResponse(compilation);
    }

    private List<Event> findEventsForCompilation(Integer compilationId) {
        List<Integer> eventIds = compilationEventRepository.findByCompilationId(compilationId);
        return eventIds.stream()
                .map(eventService::findEvent)
                .toList();
    }

    public List<CompilationResponseDto> getCompilations(Boolean pinned, int from, int size) {
        if (pinned == null) {
            return compilationRepository.findAllLimit(from, size).stream()
                    .map(this::prepareForResponse).toList();
        } else {
            return compilationRepository.findAllPinnedLimit(pinned, from, size).stream()
                    .map(this::prepareForResponse).toList();
        }
    }

    public CompilationResponseDto getCompilation(int id) {
        return prepareForResponse(findCompilation(id));
    }

    public void deleteCompilation(int id) {
        findCompilation(id);
        compilationRepository.deleteById(id);
    }

    private Compilation findCompilation(int id) {
        Optional<Compilation> compilation = compilationRepository.findById(id);
        if (compilation.isPresent()) {
            return compilation.get();
        } else {
            throw new NotFoundException("Compilation with id=" + id + " was not found");
        }
    }

    public CompilationResponseDto updateCompilation(int id, CompilationRequestDto compilationRequestDto) {
        if (compilationRequestDto.getTitle() != null
            && compilationRequestDto.getTitle().length() > MAX_COMPILATION_TITLE_LENGTH) {
            throw new BadRequestException("Compilation title cannot be greater than " + MAX_COMPILATION_TITLE_LENGTH +
                    ". It is: " + compilationRequestDto.getTitle().length());
        }
        Compilation oldCompilation = findCompilation(id);
        if (compilationRequestDto.getPinned() != null) {
            oldCompilation.setPinned(compilationRequestDto.getPinned());
        }
        if (compilationRequestDto.getTitle() != null) {
            oldCompilation.setTitle(compilationRequestDto.getTitle());
        }
        if (compilationRequestDto.getEvents() != null && !compilationRequestDto.getEvents().isEmpty()) {
            List<Integer> oldEventsIds = new ArrayList<>(findEventsForCompilation(id).stream().map(Event::getId).toList());
            Collections.sort(oldEventsIds);
            List<Integer> newEventsIds = compilationRequestDto.getEvents();
            Collections.sort(newEventsIds);
            if (!(oldEventsIds.equals(newEventsIds))) {
                compilationEventRepository.deleteByCompilationId(id);
                saveCompilationEvents(oldCompilation, newEventsIds);
            }
        }
        return prepareForResponse(compilationRepository.save(oldCompilation));
    }

    private CompilationResponseDto prepareForResponse(Compilation compilation) {
        CompilationResponseDto response = CompilationMapper.toCompilationResponseDto(compilation);
        response.setEvents(findEventsForCompilation(response.getId()).stream()
                .map(EventMapper::toEventShortDto).toList());
        return response;
    }

    private void saveCompilationEvents(Compilation compilation, List<Integer> eventsIds) {
        List<Event> events = eventsIds.stream()
                .map(eventService::findEvent)
                .toList();
        events.stream()
                .map(e -> CompilationEvent.builder().event(e).compilation(compilation).build())
                .peek(compilationEventRepository::save)
                .toList();
    }

    private void validateNewCompilation(CompilationRequestDto compilationRequestDto) {
        if (compilationRequestDto.getTitle() == null || compilationRequestDto.getTitle().isBlank()) {
            throw new BadRequestException("Compilation title cannot be null or empty");
        }
        if (compilationRequestDto.getTitle().length() > MAX_COMPILATION_TITLE_LENGTH) {
            throw new BadRequestException("Compilation title cannot be greater than " + MAX_COMPILATION_TITLE_LENGTH +
                    ". It is: " + compilationRequestDto.getTitle().length());
        }
    }
}

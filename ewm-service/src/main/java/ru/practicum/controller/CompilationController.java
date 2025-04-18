package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.CompilationRequestDto;
import ru.practicum.model.dto.CompilationResponseDto;
import ru.practicum.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompilationController {
    private final CompilationService compilationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/compilations")
    public CompilationResponseDto postCompilation(@RequestBody CompilationRequestDto compilationRequestDto) {
        return compilationService.addCompilation(compilationRequestDto);
    }

    @DeleteMapping("/admin/compilations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable int id) {
        compilationService.deleteCompilation(id);
    }

    @PatchMapping("/admin/compilations/{id}")
    public CompilationResponseDto patchCompilation(@PathVariable int id,
                                                   @RequestBody CompilationRequestDto compilationRequestDto) {
        return compilationService.updateCompilation(id, compilationRequestDto);
    }

    @GetMapping("/compilations")
    public List<CompilationResponseDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                        @RequestParam(required = false, defaultValue = "0") int from,
                                                        @RequestParam(required = false, defaultValue = "10") int size) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{id}")
    public CompilationResponseDto getCompilation(@PathVariable int id) {
        return compilationService.getCompilation(id);
    }
}

package ru.practicum.mapper;

import ru.practicum.model.Compilation;
import ru.practicum.model.dto.CompilationRequestDto;
import ru.practicum.model.dto.CompilationResponseDto;

public class CompilationMapper {
    public static Compilation fromCompilationRequestDto(CompilationRequestDto compilationRequestDto) {
        return Compilation.builder()
                .title(compilationRequestDto.getTitle())
                .pinned(compilationRequestDto.getPinned())
                .build();
    }

    public static CompilationResponseDto toCompilationResponseDto(Compilation compilation) {
        return CompilationResponseDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}

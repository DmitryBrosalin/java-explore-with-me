package ru.practicum.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationResponseDto {
    private int id;
    private List<EventShortDto> events;
    private boolean pinned;
    private String title;
}

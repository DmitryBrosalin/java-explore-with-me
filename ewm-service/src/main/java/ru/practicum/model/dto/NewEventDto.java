package ru.practicum.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewEventDto {
    private String annotation;
    private int category;
    private String description;
    private String eventDate;
    private LocationDto location;
    private boolean paid;
    private int participantLimit;
    private Boolean requestModeration;
    private String title;
}

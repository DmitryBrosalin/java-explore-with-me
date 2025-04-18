package ru.practicum.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestDto {
    private int id;
    private int event;
    private int requester;
    private String status;
    private String created;
}

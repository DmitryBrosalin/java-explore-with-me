package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "compilation_event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "compilation_id")
    private Compilation compilation;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}
